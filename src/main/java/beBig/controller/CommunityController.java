package beBig.controller;

import beBig.dto.LikeRequestDto;
import beBig.exception.NoContentFoundException;
import beBig.service.CommunityService;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Autowired
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
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
            throw new NoContentFoundException("No content found for the given filters.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    // 게시글 상세조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostVo> detail(@PathVariable long postId) throws NoHandlerFoundException {
        PostVo detail = communityService.showDetail(postId);
        if(detail == null) {
            throw new NoHandlerFoundException("GET", "/community/" + postId, null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(detail);
    }

    @PostMapping("/write")
    public ResponseEntity<String> write() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    // 좋아요/좋아요 취소 처리
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> like(@PathVariable long postId, @RequestBody LikeRequestDto likeRequestDto) throws NoHandlerFoundException{
        long postWriterNo = likeRequestDto.getPostWriterNo();
        if(postWriterNo < 1) {
            throw new NoHandlerFoundException("POST", "/" + postId + "/like", null);
        }
        communityService.updateLike(postWriterNo, postId);
        return ResponseEntity.status(HttpStatus.OK).body("Like status updated successfully!");
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
