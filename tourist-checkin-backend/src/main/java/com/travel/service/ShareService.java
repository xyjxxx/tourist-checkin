package com.travel.service;

import com.travel.dto.PosterGenDTO;
import com.travel.entity.CheckIn;
import com.travel.mapper.CheckInMapper;
import com.travel.utils.QrCodeUtil;
import com.travel.vo.CheckInVO;
import com.travel.vo.PosterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final CheckInMapper checkInMapper;
    private final QrCodeUtil qrCodeUtil;

    public PosterVO generatePoster(Long checkInId, PosterGenDTO dto) {
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null) {
            throw new RuntimeException("打卡记录不存在");
        }

        PosterVO poster = new PosterVO();
        poster.setCheckInId(checkInId);
        poster.setLocationName(checkIn.getLocationName());
        poster.setContent(checkIn.getContent());
        poster.setImageUrl(checkIn.getImages() != null && !checkIn.getImages().isEmpty()
                ? checkIn.getImages().split(",")[0] : null);
        poster.setCheckInTime(checkIn.getCheckInTime());

        String qrUrl = "https://travel.app/checkin/" + checkInId;
        poster.setQrCode(qrCodeUtil.generateBase64QrCode(qrUrl, 200, 200));

        return poster;
    }

    public String getShareLink(Long checkInId) {
        return "https://travel.app/share/checkin/" + checkInId;
    }
}
