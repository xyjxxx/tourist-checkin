package com.travel.controller;

import com.travel.service.ContentAuditService;
import com.travel.vo.ImageAuditVO;
import com.travel.vo.ReportVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ContentAuditController {

    private final ContentAuditService contentAuditService;

    @GetMapping("/reports")
    public Result<List<ReportVO>> listReports(@RequestParam(defaultValue = "-1") int status,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.success(contentAuditService.listReports(status, page, size));
    }

    @PutMapping("/report/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id,
                                      @RequestParam int status,
                                      @RequestParam String result,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contentAuditService.handleReport(userId, id, status, result);
        return Result.success();
    }

    @GetMapping("/image-audits")
    public Result<List<ImageAuditVO>> listImageAudits(@RequestParam(defaultValue = "-1") int status,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(contentAuditService.listImageAudits(status, page, size));
    }

    @PutMapping("/image-audit/{id}")
    public Result<Void> auditImage(@PathVariable Long id,
                                    @RequestParam int status,
                                    @RequestParam String result,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contentAuditService.auditImage(userId, id, status, result);
        return Result.success();
    }

    @GetMapping("/sensitive-words")
    public Result<List<String>> listSensitiveWords(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "50") int size) {
        return Result.success(contentAuditService.listSensitiveWords(page, size));
    }

    @PostMapping("/sensitive-word")
    public Result<Void> addSensitiveWord(@RequestParam String word,
                                          @RequestParam String category,
                                          @RequestParam(defaultValue = "1") int level) {
        contentAuditService.addSensitiveWord(word, category, level);
        return Result.success();
    }

    @DeleteMapping("/sensitive-word/{id}")
    public Result<Void> removeSensitiveWord(@PathVariable Long id) {
        contentAuditService.removeSensitiveWord(id);
        return Result.success();
    }
}
