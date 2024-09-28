package beBig.service;

import beBig.dto.response.PostResponseDto;
import beBig.exception.AmazonS3UploadException;
import beBig.exception.NoContentFoundException;
import beBig.mapper.CommunityMapper;
import beBig.mapper.ImageMapper;
import beBig.mapper.UserMapper;
import beBig.vo.PostVo;
import beBig.vo.UserVo;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityServiceImp implements CommunityService {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket; // S3

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSizeString;

    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CommunityServiceImp(SqlSessionTemplate sqlSessionTemplate, AmazonS3 amazonS3) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.amazonS3 = amazonS3;
    }


    /**
     * 게시글 전체 조회 및 검색 필터 조회
     *
     * @param postCategory 카테고리 필터 (없을 경우 기본값 -1)
     * @param finTypeCode 유형 필터 (없을 경우 기본값 -1)
     * @return 필터에 맞는 게시글 목록
     */
    @Override
    public List<PostVo> showList(int postCategory, int finTypeCode) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);

        Map<String, Object> params = new HashMap<>();
        if(postCategory != -1){
            // 카테고리 추가
            log.info("service category: " + postCategory);
            params.put("postCategory", postCategory);
        }
        if(finTypeCode != -1){
            // 유형 추가
            log.info("service finTypeCode: " + finTypeCode);
            params.put("finTypeCode", finTypeCode);
        }
        // 전체 목록 조회(파라미터에 검색 필터가 없는 경우)
        if (params.isEmpty()) {
            return mapper.findAll();
        }
        // 카테고리/유형별 조회(파라미터 검색 필터가 있는 경우)
        else {
            return mapper.findByPostCategoryAndFinTypeCode(params);
        }
    }

    /**
     * 게시글 상세 조회
     * - post, image, like 테이블 조회
     * @param postId 게시글 ID
     * @return 게시글 상세정보
     */
    @Override
    public PostVo showDetail(Long postId) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        log.info("Post table get");
        PostVo detail = mapper.findDetail(postId);
        log.info("detail{}",detail);

        return detail;
    }

    @Override
    public PostResponseDto convertToDto(PostVo post, boolean isUserId) {
        PostResponseDto postResponseDto = new PostResponseDto();
        postResponseDto.setPostId(post.getPostId());
        postResponseDto.setUserId(post.getUserId());
        postResponseDto.setPostTitle(post.getPostTitle());
        postResponseDto.setPostContent(post.getPostContent());
        postResponseDto.setPostCreatedTime(post.getPostCreatedTime());
        postResponseDto.setPostUpdatedTime(post.getPostUpdatedTime());
        postResponseDto.setPostLikeHits(post.getPostLikeHits());
        postResponseDto.setPostCategory(post.getPostCategory());
        postResponseDto.setFinTypeCode(post.getFinTypeCode());
        postResponseDto.setPostImagePaths(post.getPostImagePaths());
        postResponseDto.setUser(isUserId);
        return postResponseDto;
    }

    @Transactional(rollbackFor = {AmazonS3Exception.class,AmazonClientException.class, NoContentFoundException.class})
    @Override
    // 게시글 업로드, 이미지 업로드
    public void write(PostVo post) throws AmazonS3UploadException, AmazonClientException, NoContentFoundException {
        //1. mapper 연결
        CommunityMapper communityMapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        //fintype 찾아오기
        UserVo user = userMapper.findByUserId(post.getUserId());
        post.setFinTypeCode(user.getFinTypeCode());
        //post Insert
        try{
            communityMapper.insert(post);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
        if(post.getFiles() != null ) {
            //2. s3에 올리기
            List<String> filePaths = imageService.saveFiles(post.getFiles());
            log.info("다중 사진 올리기 완료");
            post.setPostImagePaths(filePaths);
            //3. image path mapper 연결
            try{
                communityMapper.insertImage(post);
            }catch (Exception e){
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private boolean isExistingImage(MultipartFile file,List<String> existingImageUrls) {
        // 예를 들어 파일 이름을 비교하거나 파일의 해시 값을 비교하는 로직
        String fileName = file.getOriginalFilename();
        return existingImageUrls.stream().anyMatch(url -> url.contains(fileName));  // 파일명이 이미 DB에 있는지 확인
    }

    /**
     * 게시글 업데이트
     * 게시글 번호에 해당하는 이미지 모두 삭제 -> 게시글 업데이트
     * @param content 업데이트할 게시글 정보
     */
    @Override
    public void update(PostVo content) {
        CommunityMapper communityMapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        ImageMapper imageMapper = sqlSessionTemplate.getMapper(ImageMapper.class);

        // DB 에 들어있는 이미지 경로 가져오고 삭제(DB, S3)
        List<String> existingImagePaths = imageMapper.findByPostId(content.getPostId());
        for(String existingImageUrl : existingImagePaths){
            imageService.deleteFile(existingImageUrl);
        }
        log.info("existingImagePaths 완료");
        imageMapper.deleteByPostId(content.getPostId());

        // 매개변수로 들어온 이미지
        List<MultipartFile> allImagePaths = content.getFiles() ;//.stream().map(MultipartFile::getOriginalFilename).toList();
        if(allImagePaths != null){
            List<String> allImageUrls = allImagePaths.stream().map(MultipartFile::getOriginalFilename).map(one -> "https://s3.ap-southeast-2.amazonaws.com/"+ bucket+ "/" + one).toList();
            log.info("allImageUrls{}",allImageUrls);
            // 새로 들어온 이미지만 업로드
            List<String> addImagePaths = allImagePaths.stream()
                    .filter(file -> !isExistingImage(file, existingImagePaths))  // 기존 이미지와 비교
                    .map(file -> {
                        try {
                            return imageService.saveFile(file);  // 새 이미지인 경우에만 업로드
                        }catch (AmazonS3UploadException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
            log.info("addImagePaths{}",addImagePaths);
            content.setPostImagePaths(addImagePaths);
            communityMapper.insertImage(content);
        }

        communityMapper.update(content);
    }

    /**
     * 좋아요/좋아요 취소 처리
     *
     * @param userId 작성자 번호
     * @param postId 게시글 ID
     */
    @Override
    public void updateLike(long userId,long postId) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("postId", postId);

        // 좋아요 눌렀는지 체크
        int likeCnt = mapper.checkLike(params);

        // 이미 좋아요를 눌렀다면 좋아요 취소
        if(likeCnt > 0){
            mapper.removeLike(params);
            // 좋아요 수 감소
            params.put("likeCnt", -1);
        }
        // 좋아요를 누르지 않았다면 좋아요 추가
        else {
            mapper.addLike(params);
            // 좋아요 수 증가
            params.put("likeCnt", 1);
        }
        // 좋아요 수 없데이트
        mapper.updateLike(params);
    }

//    /**
//     * 게시글의 작성자 ID 조회
//     *
//     * @param postId 게시글 ID
//     * @return 게시글 작성자의 user_id
//     */
//    @Override
//    public String getPostWriterId(Long postId) {
//        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
//        return mapper.getPostWriterId(postId);
//    }

    /**
     * post삭제
     * ON DELETE CASCADE : image, like_hits
     * @param postId : 게시글 아이디
     */
    @Override
    public void delete(long userId, long postId){
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        PostVo post = mapper.findDetail(postId);

        // 게시글 작성자의 userId와 로그인(요청) userId 비교
        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("not matched user");
        }

        log.info("userId" + userId);
        mapper.delete(postId);

    }
}
