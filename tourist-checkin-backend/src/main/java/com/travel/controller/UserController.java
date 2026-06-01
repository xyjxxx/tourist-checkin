package com.travel.controller;

import com.travel.dto.UserLoginDTO;
import com.travel.dto.UserRegisterDTO;
import com.travel.exception.BadRequestException;
import com.travel.service.UserService;
import com.travel.utils.RateLimitUtil;
import com.travel.vo.Result;
import com.travel.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RateLimitUtil rateLimitUtil;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Valid UserRegisterDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        if (!rateLimitUtil.isAllowed("register:" + ip, 5, Duration.ofMinutes(10))) {
            throw new BadRequestException("注册请求过于频繁，请稍后再试");
        }
        String token = userService.register(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid UserLoginDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        if (!rateLimitUtil.isAllowed("login:" + ip, 10, Duration.ofMinutes(5))) {
            throw new BadRequestException("登录请求过于频繁，请稍后再试");
        }
        String token = userService.login(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 获取其他用户的公开资料
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserProfile(@PathVariable Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    /**
     * 注销账号（需要验证密码）
     */
    @PostMapping("/delete-account")
    public Result<Void> deleteAccount(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String password = params.get("password");

        if (password == null || password.isEmpty()) {
            return Result.error("请输入密码确认");
        }

        // 验证密码
        if (!userService.verifyPassword(userId, password)) {
            return Result.error("密码错误");
        }

        userService.deleteAccount(userId);
        return Result.success();
    }

    /**
     * 更新头像
     */
    @PutMapping("/avatar")
    public Result<String> updateAvatar(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String avatarUrl = params.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            throw new BadRequestException("头像URL不能为空");
        }
        userService.updateAvatar(userId, avatarUrl);
        return Result.success(avatarUrl);
    }

    /**
     * 修改昵称
     */
    @PutMapping("/username")
    public Result<Void> updateUsername(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String newUsername = params.get("username");
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new BadRequestException("昵称不能为空");
        }
        userService.updateUsername(userId, newUsername);
        return Result.success();
    }

    /**
     * 更新背景图
     */
    @PutMapping("/background")
    public Result<String> updateBackground(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String imageUrl = params.get("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new BadRequestException("图片URL不能为空");
        }
        userService.updateBackgroundImage(userId, imageUrl);
        return Result.success(imageUrl);
    }

    /**
     * 找回/重置密码
     */
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String ip = getClientIp(request);
        if (!rateLimitUtil.isAllowed("forgot_pwd:" + ip, 3, Duration.ofMinutes(10))) {
            throw new BadRequestException("密码重置请求过于频繁，请稍后再试");
        }

        String account = params.get("account");
        String email = params.get("email");
        String newPassword = params.get("newPassword");

        if (account == null || email == null || newPassword == null) {
            throw new BadRequestException("参数不完整");
        }

        userService.forgotPassword(account, email, newPassword);
        return Result.success();
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取用户列表（管理员）
     */
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");

        List<UserVO> list = userService.getUserList(page, size, keyword);
        long total = userService.getUserCount(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 管理员删除用户
     */
    @DeleteMapping("/admin/{userId}")
    public Result<Void> adminDeleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        userService.adminDeleteUser(adminId, userId);
        return Result.success();
    }

    /**
     * 管理员编辑用户
     */
    @PutMapping("/admin/{userId}")
    public Result<Void> adminUpdateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        String email = params.get("email");
        String role = params.get("role");
        userService.adminUpdateUser(adminId, userId, email, role);
        return Result.success();
    }

    /**
     * 管理员重置用户密码
     */

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
    @PostMapping("/admin/{userId}/reset-password")
    public Result<Void> adminResetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        String newPassword = params.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }
        userService.adminResetPassword(adminId, userId, newPassword);
        return Result.success();
    }
}
