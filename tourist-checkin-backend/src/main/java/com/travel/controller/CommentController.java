package com.travel.controller;

import com.travel.dto.CommentCreateDTO;
import com.travel.service.CommentService;
import com.travel.utils.AuthUtil;
import com.travel.vo.CommentVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> create(@RequestBody @Valid CommentCreateDTO dto, HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(commentService.create(userId, dto));
    }

    @GetMapping("/page")
    public Result<List<CommentVO>> page(@RequestParam(required = false) Long checkInId,
                                         @RequestParam(required = false) Long noteId,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         HttpServletRequest request) {
        if (checkInId == null && noteId == null) {
            throw new com.travel.exception.BadRequestException("checkInId 或 noteId 不能为空");
        }
        Long userId = requireUserId(request);
        if (noteId != null) {
            return Result.success(commentService.pageByNote(noteId, userId, page, size));
        }
        return Result.success(commentService.pageByCheckIn(checkInId, userId, page, size));
    }

    @PostMapping("/{commentId}/like")
    public Result<Void> like(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = requireUserId(request);
        commentService.like(userId, commentId);
        return Result.success();
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = requireUserId(request);
        commentService.delete(userId, commentId);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.adminList(page, size));
    }

    @DeleteMapping("/admin/{commentId}")
    public Result<Void> adminDelete(@PathVariable Long commentId) {
        commentService.adminDelete(commentId);
        return Result.success();
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
