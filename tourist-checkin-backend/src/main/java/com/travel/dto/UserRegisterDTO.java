package com.travel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 20, message = "账号长度3-20位")
    private String account;

    @Size(max = 20, message = "昵称最长20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;
}
