package com.travel.controller;

import com.travel.dto.ReportCreateDTO;
import com.travel.service.ContentAuditService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ContentAuditService contentAuditService;

    /**
     * 普通用户提交举报（原位于 /api/admin/report，因路径被误拦截为管理员接口而迁移）
     */
    @PostMapping
    public Result<Void> submitReport(@RequestBody @Valid ReportCreateDTO dto,
                                      HttpServletRequest request) {
        Long userId = requireUserId(request);
        contentAuditService.submitReport(userId, dto);
        return Result.success();
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
