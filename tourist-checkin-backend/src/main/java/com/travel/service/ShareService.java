package com.travel.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.travel.dto.PosterGenDTO;
import com.travel.entity.CheckIn;
import com.travel.mapper.CheckInMapper;
import com.travel.utils.QrCodeUtil;
import com.travel.vo.CheckInVO;
import com.travel.vo.PosterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final CheckInMapper checkInMapper;

    @Value("${app.share-domain:https://travel.app}")
    private String shareDomain;

    public PosterVO generatePoster(Long checkInId, PosterGenDTO dto) {
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null) {
            throw new com.travel.exception.BadRequestException("打卡记录不存在");
        }

        PosterVO poster = new PosterVO();
        poster.setCheckInId(checkInId);
        poster.setLocationName(checkIn.getLocationName());
        poster.setContent(checkIn.getContent());
        String firstImage = null;
        if (checkIn.getImages() != null && !checkIn.getImages().isEmpty()) {
            try {
                JSONArray arr = JSON.parseArray(checkIn.getImages());
                if (arr != null && !arr.isEmpty()) {
                    firstImage = arr.getString(0);
                }
            } catch (Exception e) {
                firstImage = checkIn.getImages().split(",")[0];
            }
        }
        poster.setImageUrl(firstImage);
        poster.setCheckInTime(checkIn.getCheckInTime());

        String qrUrl = shareDomain + "/checkin/" + checkInId;
        poster.setQrCode(QrCodeUtil.generateBase64QrCode(qrUrl, 200, 200));

        return poster;
    }

    public String getShareLink(Long checkInId) {
        return shareDomain + "/share/checkin/" + checkInId;
    }
}
