package beBig.controller;

import beBig.exception.AmazonS3UploadException;
import beBig.service.CommunityService;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin("*")
@Controller
@RequestMapping("/community")
@Slf4j
public class CommunityController {
    private CommunityService communityService;

    @Autowired
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("")
    public ResponseEntity<String> list() {
        log.info("get community list");
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{postId}")
    public ResponseEntity<String> detail(@PathVariable Long postId) {
        log.info("get community detail");
        return ResponseEntity.status(HttpStatus.OK).body(postId.toString());
    }

    @PostMapping("/write")
    public String write( PostVo content) throws AmazonS3UploadException {
        log.info("write community");
//        PostVo content = new PostVo();
//        content.setPostWriterNo((long) (int) map.get("postWriterNo"));
//        content.setPostTitle((String) map.get("postTitle"));
//        content.setPostContent((String) map.get("postContent"));
//        content.setPostWriterFinTypeCode((int) map.get("postWriterFinTypeCode"));
//        content.setPostImagePath(map.get("postImagePath").toString());
//        content.setPostCategory((Integer) map.get("postCategory"));
//        content.setFile(file);

        //TODO fileList 처리
//        List<MultipartFile> fileList = (List<MultipartFile>) map.get("fileList");
        log.info("content",content);
        communityService.write(content);
        return "ResponseEntity.ok().build();";
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> like(@PathVariable Long postId) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/{postId}/update")
    public ResponseEntity<String> update(@PathVariable Long postId) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @DeleteMapping("/{postId}/delete")
    public ResponseEntity<String> delete(@PathVariable Long postId) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }
}
