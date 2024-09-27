package beBig.service;

import beBig.exception.AmazonS3UploadException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.log4j.NDC.clear;
@Service
@Slf4j
public class S3ImageServiceImp implements ImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket; // S3

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSizeString;

    public S3ImageServiceImp(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }


    /**
     * S3에 여러 사진 올리기
     * @param files : File List
     * @return : File Path List
     * @throws AmazonS3UploadException : upload error
     */
    @Override
    public List<String> saveFiles(List<MultipartFile> files) throws AmazonS3UploadException {
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String uploadedUrl = saveFile(file);
            uploadedUrls.add(uploadedUrl);
        }
        clear();
        return uploadedUrls;
    }

    /**
     * S3에 사진 각 올리기
     * metaData 추가됨
     * @param file : MultipartFile type 파일
     * @return : filePath
     * @throws AmazonS3UploadException : upload error
     */
    @Override
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

        log.info("File upload completed: fileName{}", fileName);

        return amazonS3.getUrl(bucket, fileName).toString();

    }

    @Override
    public void deleteFile(String fileUrl){
        amazonS3.deleteObject(bucket,fileUrl);
    }
}
