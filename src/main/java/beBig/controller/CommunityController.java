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
    public ResponseEntity<String> update(@PathVariable Long postId, @RequestBody PostVo content){
        // 게시글 업데이트
        log.info("postId : " + postId);
        communityService.update(content);
        return ResponseEntity.status(HttpStatus.OK).body("successfully update");
    }

    @DeleteMapping("/{postId}/delete")
    public ResponseEntity<String> delete(@PathVariable Long postId) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }
}
