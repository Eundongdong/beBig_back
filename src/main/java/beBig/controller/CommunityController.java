package beBig.controller;

import beBig.dto.response.PostResponseDto;
import beBig.exception.AmazonS3UploadException;
import beBig.exception.NoContentFoundException;
import beBig.service.CommunityService;
import beBig.service.jwt.JwtUtil;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@Controller
@RequestMapping("/community")
@Slf4j
public class CommunityController {
    private CommunityService communityService;
    private JwtUtil jwtUtil;

    @Autowired
    public CommunityController(CommunityService communityService, JwtUtil jwtUtil) {
        this.communityService = communityService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping()
    public ResponseEntity<List<PostVo>> list(@RequestParam(value = "category", required = false) Optional<Integer> postCategory,
                                             @RequestParam(value = "type", required = false) Optional<Integer> finTypeCode) {
        // Optional에서 값이 없을 경우 -1로 처리
        int category = postCategory.orElse(-1);
        log.info("category: " + category);
        int type = finTypeCode.orElse(-1);
        log.info("type: " + type);

        List<PostVo> list = communityService.showList(category, type);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> detail(@RequestHeader("Authorization") String token,
                                         @PathVariable long postId) throws NoHandlerFoundException {
        Long userId = jwtUtil.extractUserIdFromToken(token);

        PostVo detail = communityService.showDetail(postId);
        if(detail == null) {
            throw new NoHandlerFoundException("GET", "/community/" + postId, null);
        }

        boolean isUserId = detail.getUserId().equals(userId);

        PostResponseDto responseDto = communityService.convertToDto(detail, isUserId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("/write")
    public ResponseEntity write(PostVo content) throws AmazonS3UploadException {
        log.info("write community");
        log.info("content{}",content);
        communityService.write(content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> like(@PathVariable long postId,@RequestBody long userId) throws NoHandlerFoundException{
        // 요청받은 게시글 작성자 번호 추출
        if(userId < 1) {
            throw new NoHandlerFoundException("POST", "/" + postId + "/like", null);
        }
        communityService.updateLike(userId, postId);
        return ResponseEntity.status(HttpStatus.OK).body("Like status updated successfully!");
    }

    @PostMapping("/{postId}/update")
    public ResponseEntity<String> update(@ModelAttribute PostVo content, @PathVariable long postId) throws AmazonS3UploadException {
        // 게시글이 존재하는지 확인
        PostVo existPost = communityService.showDetail(content.getPostId());
        if(existPost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No content found for the given filters.");
        }

        // 게시글 내용 검증
        if (content.getPostTitle() == null || content.getPostTitle().trim().isEmpty() ||
                content.getPostContent() == null || content.getPostContent().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("input the title or content");
        }
        // 게시글 번호 확인
        log.info("postId : " + postId);
         //게시글 업데이트
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
