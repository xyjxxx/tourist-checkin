package com.travel.controller;

import com.travel.service.TopicService;
import com.travel.vo.Result;
import com.travel.vo.TopicVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                                         @RequestBody List<Long> topicIds) {
        topicService.attachToCheckIn(checkInId, topicIds);
        return Result.success();
    }
}
