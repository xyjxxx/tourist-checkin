package com.travel.controller;

import com.travel.service.ContentAuditService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ContentAuditController {

    private final ContentAuditService contentAuditService;

    @GetMapping("/reports")
    public Result<Map<String, Object>> listReports(@RequestParam(defaultValue = "-1") int status,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return Result.success(contentAuditService.listReports(status, page, size));
    }

    @PutMapping("/report/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id,
                                      @RequestBody java.util.Map<String, Object> body,
                                      HttpServletRequest request) {
        Long userId = requireUserId(request);
        int status;
        try {
            status = body.get("status") != null ? Integer.parseInt(body.get("status").toString()) : 0;
        } catch (NumberFormatException e) {
            throw new com.travel.exception.BadRequestException("status 参数必须为数字");
        }
        String result = body.get("handleResult") != null ? body.get("handleResult").toString() : "";
        contentAuditService.handleReport(userId, id, status, result);
        return Result.success();
    }

    @GetMapping("/image-audits")
    public Result<Map<String, Object>> listImageAudits(@RequestParam(defaultValue = "-1") int status,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.success(contentAuditService.listImageAudits(status, page, size));
    }

    @PutMapping("/image-audit/{id}")
    public Result<Void> auditImage(@PathVariable Long id,
                                    @RequestParam int status,
                                    @RequestParam String result,
                                    HttpServletRequest request) {
        Long userId = requireUserId(request);
        contentAuditService.auditImage(userId, id, status, result);
        return Result.success();
    }

    @GetMapping("/sensitive-words")
    public Result<Map<String, Object>> listSensitiveWords(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "50") int size,
                                                           HttpServletRequest request) {
        requireUserId(request);
        return Result.success(contentAuditService.listSensitiveWords(page, size));
    }

    @PostMapping("/sensitive-word")
    public Result<Void> addSensitiveWord(@RequestParam String word,
                                          @RequestParam String category,
                                          @RequestParam(defaultValue = "1") int level,
                                          HttpServletRequest request) {
        requireUserId(request);
        contentAuditService.addSensitiveWord(word, category, level);
        return Result.success();
    }

    @DeleteMapping("/sensitive-word/{id}")
    public Result<Void> removeSensitiveWord(@PathVariable Long id, HttpServletRequest request) {
        requireUserId(request);
        contentAuditService.removeSensitiveWord(id);
        return Result.success();
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
