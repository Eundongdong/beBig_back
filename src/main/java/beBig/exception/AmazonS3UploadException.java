package beBig.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmazonS3UploadException extends Exception {

    public AmazonS3UploadException(AmazonS3Exception amazonS3Exception) {
        super(amazonS3Exception);
    }
    public AmazonS3UploadException(String message) {
        super(message);
        log.error("Amazon S3 Upload Exception: {}", message);
    }



//    public  AmazonS3UploadException(Exception e, String type) {
//        switch (type) {
//            case "AmazonS3Exception":
//                log.error("Amazon S3 error while uploading file: " + e.getMessage());
//                super(type);
////                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//            case "AmazonClientException":
//                log.error("AWS SDK client error while uploading file: " + e.getMessage());
//                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//            case "SdkClientException":
//                return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
//            default:
//                log.error("IO error while uploading file: " + e.getMessage());
//                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//
//    }
}
