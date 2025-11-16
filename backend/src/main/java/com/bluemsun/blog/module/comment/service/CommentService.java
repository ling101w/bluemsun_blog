package com.bluemsun.blog.module.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.blog.module.comment.dto.CommentCreateDTO;
import com.bluemsun.blog.module.comment.entity.Comment;
import com.bluemsun.blog.module.comment.vo.CommentTreeVO;

import java.util.List;

/**
 * 评论服务。
 */
public interface CommentService extends IService<Comment> {

    Long createComment(Long userId, CommentCreateDTO request);

    void likeComment(Long commentId, Long userId);

    List<CommentTreeVO> listTreeByArticle(Long articleId);

    String previewEmoji(String content);
}

