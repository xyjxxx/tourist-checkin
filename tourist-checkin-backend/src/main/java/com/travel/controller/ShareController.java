package com.travel.controller;

import com.travel.dto.PosterGenDTO;
import com.travel.service.ShareService;
import com.travel.vo.PosterVO;
import com.travel.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/poster")
    public Result<PosterVO> generatePoster(@RequestBody @Valid PosterGenDTO dto) {
        return Result.success(shareService.generatePoster(dto.getCheckInId(), dto));
    }

    @GetMapping("/link/{checkInId}")
    public Result<String> shareLink(@PathVariable Long checkInId) {
        return Result.success(shareService.getShareLink(checkInId));
    }
}
