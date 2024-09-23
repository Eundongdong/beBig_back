package beBig.controller;

import beBig.service.CommunityService;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.Access;
import java.util.List;

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
    public ResponseEntity<List<PostVo>> list() {
        List<PostVo> list = communityService.showList();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostVo> detail(@PathVariable Long postId) throws NoHandlerFoundException {
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
