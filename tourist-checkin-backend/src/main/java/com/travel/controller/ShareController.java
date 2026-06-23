package com.travel.controller;

import com.travel.dto.PosterGenDTO;
import com.travel.service.ShareService;
import com.travel.utils.AuthUtil;
import com.travel.vo.PosterVO;
import com.travel.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    private Long requireUserId(HttpServletRequest request) {
        return AuthUtil.requireUserId(request);
    }

    @PostMapping("/poster")
    public Result<PosterVO> generatePoster(@RequestBody @Valid PosterGenDTO dto,
                                            HttpServletRequest request) {
        requireUserId(request);
        return Result.success(shareService.generatePoster(dto.getCheckInId(), dto));
    }

    @GetMapping("/link/{checkInId}")
    public Result<String> shareLink(@PathVariable Long checkInId,
                                     HttpServletRequest request) {
        requireUserId(request);
        return Result.success(shareService.getShareLink(checkInId));
    }
}
