package com.bluemsun.blog.module.comment.convert;

import com.bluemsun.blog.module.comment.entity.Comment;
import com.bluemsun.blog.module.comment.vo.CommentTreeVO;
import com.bluemsun.blog.module.user.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论转换工具。
 */
public final class CommentConvert {

    private CommentConvert() {
    }

    public static CommentTreeVO toTreeVO(Comment comment, User author) {
        CommentTreeVO vo = new CommentTreeVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        vo.setLikeCount(comment.getLikeCount());
        if (author != null) {
            vo.setNickname(author.getNickname());
            vo.setAvatar(author.getAvatar());
        }
        return vo;
    }

    public static Map<Long, User> indexUser(List<User> users) {
        Map<Long, User> map = new HashMap<>(users.size());
        for (User user : users) {
            map.put(user.getId(), user);
        }
        return map;
    }
}

