package beBig.controller;

import beBig.exception.AmazonS3UploadException;
import org.apache.ibatis.annotations.Delete;
import beBig.exception.NoContentFoundException;
import beBig.service.CommunityService;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Optional;

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

    // 게시글 전체 조회 & 검색 필터 조회
    @GetMapping()
    public ResponseEntity<List<PostVo>> list(@RequestParam(value = "category", required = false) Optional<Integer> postCategory,
                                             @RequestParam(value = "type", required = false) Optional<Integer> postWriterFinTypeCode) {
        // Optional에서 값이 없을 경우 -1로 처리
        int category = postCategory.orElse(-1);
        int type = postWriterFinTypeCode.orElse(-1);

        List<PostVo> list = communityService.showList(category, type);
        if (list == null || list.isEmpty()) {
            throw new NoContentFoundException("조회된 게시글이 없습니다.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    // 게시글 상세조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostVo> detail(@PathVariable Long postId) throws NoHandlerFoundException {
        PostVo detail = communityService.showDetail(postId);
        if(detail == null) {
            throw new NoHandlerFoundException("GET", "/community/" + postId, null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(detail);
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
