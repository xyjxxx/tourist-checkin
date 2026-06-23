package com.travel.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ShopRecommendCreateDTO {
    @NotBlank(message = "店铺名称不能为空")
    private String name;

    @NotBlank(message = "请选择菜系分类")
    private String category;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    @NotNull(message = "请选择店铺位置")
    private BigDecimal longitude;

    @NotNull(message = "请选择店铺位置")
    private BigDecimal latitude;

    private String city;

    @Min(value = 0, message = "人均价位不能为负")
    private Integer avgPrice;

    @NotEmpty(message = "请上传至少3张实拍图片")
    @Size(min = 3, message = "请上传至少3张实拍图片")
    private List<String> images;

    @NotBlank(message = "请输入推荐理由")
    @Size(min = 10, message = "推荐理由至少10个字")
    private String recommendReason;

    private String signatureDish;
    private String businessHours;
    private String phone;
    private String warning;
}
