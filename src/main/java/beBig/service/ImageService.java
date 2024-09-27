package beBig.service;

import beBig.exception.AmazonS3UploadException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    public List<String> saveFiles(List<MultipartFile> files) throws AmazonS3UploadException;
    public String saveFile(MultipartFile file) throws AmazonS3UploadException;
    public void deleteFile(String fileUrl);
}
