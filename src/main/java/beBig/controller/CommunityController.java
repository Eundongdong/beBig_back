package beBig.controller;

import beBig.dto.LikeRequestDto;
import beBig.exception.AmazonS3UploadException;
import beBig.exception.NoContentFoundException;
import beBig.service.CommunityService;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;
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

    @GetMapping()
    public ResponseEntity<List<PostVo>> list(@RequestParam(value = "category", required = false) Optional<Integer> postCategory,
                                             @RequestParam(value = "type", required = false) Optional<Integer> postWriterFinTypeCode) {
        // Optional에서 값이 없을 경우 -1로 처리
        int category = postCategory.orElse(-1);
        int type = postWriterFinTypeCode.orElse(-1);

        List<PostVo> list = communityService.showList(category, type);
        if (list == null || list.isEmpty()) {
            throw new NoContentFoundException("No content found for the given filters.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostVo> detail(@PathVariable long postId) throws NoHandlerFoundException {
        PostVo detail = communityService.showDetail(postId);
        if(detail == null) {
            throw new NoHandlerFoundException("GET", "/community/" + postId, null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(detail);
    }

    @PostMapping("/write")
    public ResponseEntity write(PostVo content) throws AmazonS3UploadException {
        log.info("write community");
        log.info("content{}",content);
        communityService.write(content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> like(@PathVariable long postId, @RequestBody LikeRequestDto likeRequestDto) throws NoHandlerFoundException{
        // 요청받은 게시글 작성자 번호 추출
        long postWriterId = likeRequestDto.getPostWriterId();
        if(postWriterId < 1) {
            throw new NoHandlerFoundException("POST", "/" + postId + "/like", null);
        }
        communityService.updateLike(postWriterId, postId);
        return ResponseEntity.status(HttpStatus.OK).body("Like status updated successfully!");
    }

    @PutMapping("/{postId}/update")
    public ResponseEntity<String> update(
            @RequestParam(value = "postTitle") String postTitle, @RequestParam(value = "postContent") String postContent,
            @RequestParam(value = "finTypeCode") Optional<Integer>  finTypeCode,
            @RequestParam(value = "postCategory") Optional<Integer>  postCategory
            ,@PathVariable long postId) throws AmazonS3UploadException {
        // 게시글이 존재하는지 확인
        PostVo existPost = communityService.showDetail(postId);
        PostVo content = new PostVo();
        content.setPostId(postId);
        content.setPostTitle(postTitle);
        content.setPostContent(postContent);
        content.setFinTypeCode(finTypeCode.orElse(-1));
        content.setPostCategory(postCategory.orElse(-1));
        log.info("content{}",content);
        if(existPost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No content found for the given filters.");
        }
        log.info("들어온 content{}",content);

        // 게시글 내용 검증
        if (content.getPostTitle() == null || content.getPostTitle().trim().isEmpty() ||
                content.getPostContent() == null || content.getPostContent().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("input the title or content");
        }
        // 게시글 번호 확인
        log.info("postId : " + postId);
        // 게시글 업데이트
        communityService.update(content);
        return ResponseEntity.status(HttpStatus.OK).body("successfully update");
    }

    @DeleteMapping("/{postId}/delete")
    public ResponseEntity delete(@PathVariable Long postId) {
        if(postId ==null){
            throw new NoContentFoundException("No content found for the given filters.");
        }
        log.info("delete community");
        communityService.delete(postId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
