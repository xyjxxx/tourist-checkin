package com.travel.controller;

import com.travel.service.TopicService;
import com.travel.utils.AuthUtil;
import com.travel.vo.Result;
import com.travel.vo.TopicVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/trending")
    public Result<List<TopicVO>> trending() {
        return Result.success(topicService.trending());
    }

    @GetMapping("/search")
    public Result<List<TopicVO>> search(@RequestParam String keyword) {
        return Result.success(topicService.search(keyword));
    }

    @PostMapping("/attach/{checkInId}")
    public Result<Void> attachToCheckIn(@PathVariable Long checkInId,
                                         @RequestBody List<Long> topicIds,
                                         HttpServletRequest request) {
        Long userId = requireUserId(request);
        topicService.attachToCheckIn(userId, checkInId, topicIds);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(topicService.adminList(page, size, keyword));
    }

    @PostMapping("/admin")
    public Result<TopicVO> adminCreate(@RequestBody Map<String, String> params) {
        return Result.success(topicService.adminCreate(
                params.get("name"), params.get("icon"), params.get("description")));
    }

    @PutMapping("/admin/{id}")
    public Result<Void> adminUpdate(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer isHot = null;
        if (params.get("isHot") != null) {
            try {
                Object val = params.get("isHot");
                isHot = (val instanceof Number) ? ((Number) val).intValue() : Integer.valueOf(val.toString());
            } catch (NumberFormatException e) {
                throw new com.travel.exception.BadRequestException("isHot 参数格式不正确");
            }
        }
        topicService.adminUpdate(id,
                params.get("name") != null ? params.get("name").toString() : null,
                params.get("icon") != null ? params.get("icon").toString() : null,
                params.get("description") != null ? params.get("description").toString() : null,
                isHot);
        return Result.success();
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        topicService.adminDelete(id);
        return Result.success();
    }

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }
}
