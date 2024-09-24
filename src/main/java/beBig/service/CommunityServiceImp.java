package beBig.service;

import beBig.exception.AmazonS3UploadException;
import beBig.mapper.CommunityMapper;
import beBig.vo.PostVo;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.log4j.NDC.clear;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityServiceImp implements CommunityService {
    private final AmazonS3 amazonS3;
    private Set<String> uploadedFileNames = new HashSet<>();
    private Set<Long> uploadedFileSizes = new HashSet<>();

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSizeString;

    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private CommunityServiceImp(SqlSessionTemplate sqlSessionTemplate, AmazonS3 amazonS3) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.amazonS3 = amazonS3;
    }


    @Override
    public List<PostVo> showList() {
        return List.of();
    }

    @Override
    public PostVo showDetail(Long postId) {
        return null;
    }

    @Override
    public void write(PostVo post) throws AmazonS3UploadException {
        //1. s3에 올리기
        //TODO 여러 사진 올리기 적용 (현재 사진 하나 처리)
        if(post.getFile() != null ) {
            String filePath = saveFile(post.getFile());
            log.info("()()()사진 올리기 완()()()");
            //2. mapper 연결
            //postImagePath 설정
            post.setPostImagePath(filePath);
        }
        CommunityMapper communityMapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        try{
            communityMapper.insert(post);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
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
        log.info("File upload started : ",fileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        try{
            amazonS3.putObject(bucket,fileName,file.getInputStream(),metadata);
        }catch (AmazonS3Exception e){
//            log.error("Amazon S3 error while uploading file: " + e.getMessage());
            throw new AmazonS3UploadException("AmazonS3Exception");
        } catch (SdkClientException e) {
//            log.error("AWS SDK client error while uploading file: " + e.getMessage());
            throw new AmazonS3UploadException("SdkClientException");
        } catch (IOException e) {
//            log.error("IO error while uploading file: " + e.getMessage());
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
