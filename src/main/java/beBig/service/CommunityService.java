package beBig.service;

import beBig.exception.AmazonS3UploadException;
import beBig.vo.PostVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommunityService {
    public List<PostVo> showList(int postCategory, int postWriterFinTypeCode);
    public PostVo showDetail(Long postId);
    public void write(PostVo post) throws AmazonS3UploadException;
    public void updateLike(Long postId);
    public void update(PostVo post);
    public void delete(Long postId);
}
