package com.travel.controller;

import com.travel.dto.CommentCreateDTO;
import com.travel.service.CommentService;
import com.travel.vo.CommentVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> create(@RequestBody @Valid CommentCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(commentService.create(userId, dto));
    }

    @GetMapping("/page")
    public Result<List<CommentVO>> page(@RequestParam Long checkInId,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(commentService.pageByCheckIn(checkInId, userId, page, size));
    }

    @PostMapping("/{commentId}/like")
    public Result<Void> like(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        commentService.like(userId, commentId);
        return Result.success();
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        commentService.delete(userId, commentId);
        return Result.success();
    }
}
