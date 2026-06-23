package com.travel.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShopRecommendVO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String name;
    private String category;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String city;
    private Integer avgPrice;
    private List<String> images;
    private String recommendReason;
    private String signatureDish;
    private String businessHours;
    private String phone;
    private String warning;
    private Integer auditStatus;
    private String auditReason;
    private Long merchantId;
    private Integer likeCount;
    private Integer collectCount;
    private Integer isFeatured;
    private Boolean hasLiked;
    private Double distance;
    private LocalDateTime createdAt;
}
