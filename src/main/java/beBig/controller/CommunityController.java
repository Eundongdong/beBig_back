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

    /**
     * 게시글 전체 조회 및 검색 필터 조회
     *
     * @param postCategory 카테고리 필터 (없을 경우 기본값 -1)
     * @param postWriterFinTypeCode 유형 필터 (없을 경우 기본값 -1)
     * @return 필터에 맞는 게시글 목록
     * @throws NoContentFoundException 필터에 맞는 게시글이 없을 때 예외 발생
     */
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

    /**
     * 게시글 상세 조회
     *
     * @param postId 게시글 ID
     * @return 게시글 상세정보
     * @throws NoHandlerFoundException 게시글을 찾지 못했을 때 예외 발생
     */
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

    /**
     * 게시글 좋아요/좋아요 취소 처리
     *
     * @param postId 게시글 ID
     * @param likeRequestDto 게시글 작성자 번호 정보
     * @return 처리 결과 메시지
     * @throws NoHandlerFoundException 잘못된 요청 시 예외 발생
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> like(@PathVariable long postId, @RequestBody LikeRequestDto likeRequestDto) throws NoHandlerFoundException{
        // 요청받은 게시글 작성자 번호 추출
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
