package beBig.service;

import beBig.exception.AmazonS3UploadException;
import beBig.exception.NoContentFoundException;
import beBig.mapper.CommunityMapper;
import beBig.vo.PostVo;
import com.amazonaws.AmazonClientException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.apache.log4j.NDC.clear;

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
    private CommunityServiceImp(SqlSessionTemplate sqlSessionTemplate, AmazonS3 amazonS3) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.amazonS3 = amazonS3;
    }


    @Override
    public List<PostVo> showList(int postCategory, int postWriterFinTypeCode) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);

        Map<String, Object> params = new HashMap<>();
        if(postCategory != -1){
            params.put("postCategory", postCategory);
        }
        if(postWriterFinTypeCode != -1){
            params.put("postWriterFinTypeCode", postWriterFinTypeCode);
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

    @Override
    public PostVo showDetail(Long postId) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        PostVo detail = mapper.findDetail(postId);
        return detail;
    }

    @Transactional(rollbackFor = {AmazonS3Exception.class,AmazonClientException.class, NoContentFoundException.class})
    @Override
    // 게시글 업로드, 이미지 업로드
    public void write(PostVo post) throws AmazonS3UploadException, AmazonClientException, NoContentFoundException {
        //1. mapper 연결
        CommunityMapper communityMapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        //post Insert
        try{
            communityMapper.insert(post);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
        if(post.getFiles() != null ) {
            //2. s3에 올리기
            List<String> filePaths = saveFiles(post.getFiles());
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

    public List<String> saveFiles(List<MultipartFile> files) throws AmazonS3UploadException {
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String uploadedUrl = saveFile(file);
            uploadedUrls.add(uploadedUrl);
        }
        clear();
        return uploadedUrls;
    }

    public String saveFile(MultipartFile file) throws AmazonS3UploadException {
        String fileName = file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        try{
            amazonS3.putObject(bucket,fileName,file.getInputStream(),metadata);
        }catch (AmazonS3Exception e){
            throw new AmazonS3UploadException("AmazonS3Exception");
        } catch (SdkClientException e) {
            throw new AmazonS3UploadException("SdkClientException");
        } catch (IOException e) {
            throw new AmazonS3UploadException("IOException");
        }

        log.info("File upload completed: " + fileName);

        return amazonS3.getUrl(bucket, fileName).toString();

    }

    @Override
    public void updateLike(Long postId) {

    }

    @Override
    public void update(PostVo post) {

    }

    @Override
    public void delete(Long postId) {

    }
}
