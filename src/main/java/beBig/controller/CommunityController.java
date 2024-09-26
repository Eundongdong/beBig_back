package beBig.controller;

import beBig.dto.LikeRequestDto;
import beBig.exception.AmazonS3UploadException;
import beBig.service.jwt.JwtTokenProvider;
import beBig.exception.NoContentFoundException;
import beBig.service.CommunityService;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@Controller
@RequestMapping("/community")
@Slf4j
public class CommunityController {
    private CommunityService communityService;
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    public CommunityController(CommunityService communityService, JwtTokenProvider jwtTokenProvider) {
        this.communityService = communityService;
        this.jwtTokenProvider = jwtTokenProvider;
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
    public ResponseEntity write( PostVo content) throws AmazonS3UploadException {
        log.info("write community");
        log.info("content{}",content);
        communityService.write(content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> like(@PathVariable long postId,long userId) throws NoHandlerFoundException{
        // 요청받은 게시글 작성자 번호 추출
        if(userId < 1) {
            throw new NoHandlerFoundException("POST", "/" + postId + "/like", null);
        }
        communityService.updateLike(userId, postId);
        return ResponseEntity.status(HttpStatus.OK).body("Like status updated successfully!");
    }

    @PutMapping("/{postId}/update")
    public ResponseEntity update(@PathVariable Long postId, @RequestBody PostVo postVo, HttpServletRequest request){
        // 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
        }

        // JWT에서 사용자 ID 추출
        String loginUserId = jwtTokenProvider.getUserIdFromJWT(token);
        // 게시글 작성자의 user_id 확인
        String postWriterId = communityService.getPostWriterId(postId);

        // 게시글 작성자와 현재 로그인한 사용자가 일치하는지 확인
        // 게시글 작성자와 현재 로그인한 사용자가 일치하는지 확인
        if (!loginUserId.equals(postWriterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("is not writer");
        }

        // 작성자 검증 통과 후 게시글 업데이트
        communityService.update(postVo);
        return ResponseEntity.status(HttpStatus.OK).body("successfully update");
    }

    // JWT 토큰을 Request 헤더에서 추출하는 메소드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거
        }
        return null;
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
