package com.bluemsun.blog.module.comment.service;

import com.bluemsun.blog.module.comment.entity.Comment;

public interface CommentNotificationService {

    void sendCommentNotice(Comment comment);
}


