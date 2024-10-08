package beBig.service;

import beBig.dto.response.PostListResponseDto;
import beBig.dto.response.PostResponseDto;
import beBig.exception.AmazonS3UploadException;
import beBig.vo.PostVo;

import java.util.List;

public interface CommunityService {
    public PostListResponseDto showList(int postCategory, int finTypeCode, int page, int pageSize);
    public PostVo showDetail(Long postId);
    public void updateLike(long userId, long postId);
    public void write(PostVo post) throws AmazonS3UploadException;
    public void update(PostVo post);
    public void delete(long userId, long postId);

    PostResponseDto convertToDto(PostVo post, boolean isUserId);
}
