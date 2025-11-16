package com.bluemsun.blog.module.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.bluemsun.blog.common.core.ApiResponse;
import com.bluemsun.blog.module.admin.vo.AdminOverviewVO;
import com.bluemsun.blog.module.article.service.ArticleService;
import com.bluemsun.blog.module.comment.service.CommentService;
import com.bluemsun.blog.module.order.service.OrderService;
import com.bluemsun.blog.module.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@SaCheckRole("ADMIN")
public class AdminStatsController {

    private final UserService userService;
    private final ArticleService articleService;
    private final CommentService commentService;
    private final OrderService orderService;

    public AdminStatsController(UserService userService,
                                ArticleService articleService,
                                CommentService commentService,
                                OrderService orderService) {
        this.userService = userService;
        this.articleService = articleService;
        this.commentService = commentService;
        this.orderService = orderService;
    }

    /**
     * 管理后台 - 概览统计
     * 返回用户/文章/评论/订单的数量汇总。
     */
    /** GET /admin/stats/overview - 获取后台仪表盘概览数据 */
    @GetMapping("/overview")
    public ApiResponse<AdminOverviewVO> overview() {
        AdminOverviewVO vo = new AdminOverviewVO();
        vo.setUserCount(userService.count());
        vo.setArticleCount(articleService.count());
        vo.setCommentCount(commentService.count());
        vo.setOrderCount(orderService.count());
        return ApiResponse.success(vo);
    }
}


