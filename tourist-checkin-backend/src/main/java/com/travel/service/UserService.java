package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.UserLoginDTO;
import com.travel.dto.UserRegisterDTO;
import com.travel.entity.User;
import com.travel.exception.BadRequestException;
import com.travel.exception.UnauthorizedException;
import com.travel.mapper.UserMapper;
import com.travel.utils.JwtUtil;
import com.travel.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(UserRegisterDTO dto) {
        // 检查账号是否已被占用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, dto.getAccount());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BadRequestException("账号已存在");
        }

        // 物理清除被软删除的同名账号
        cleanDeletedAccount(dto.getAccount());

        User user = new User();
        user.setAccount(dto.getAccount());
        // 昵称默认为账号，如果提供了昵称则使用昵称
        user.setUsername(dto.getUsername() != null && !dto.getUsername().isEmpty()
                ? dto.getUsername() : dto.getAccount());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole("USER"); // 默认普通用户
        userMapper.insert(user);

        return jwtUtil.generateToken(user.getId(), user.getAccount(), user.getRole());
    }

    private void cleanDeletedAccount(String account) {
        try {
            userMapper.physicallyDeleteByAccount(account);
        } catch (Exception e) {
            // 忽略清理失败，不影响注册流程
        }
    }

    public String login(UserLoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, dto.getAccount());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new UnauthorizedException("账号或密码错误");
        }

        boolean matches;
        try {
            matches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("账号或密码错误");
        }

        if (!matches) {
            throw new UnauthorizedException("账号或密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getAccount(), user.getRole());
    }

    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 获取用户公开资料（隐藏敏感信息）
     */
    public UserVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setEmail(null); // 隐藏邮箱
        return vo;
    }

    /**
     * 更新背景图
     */
    public void updateBackgroundImage(Long userId, String imageUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        user.setBackgroundImage(imageUrl);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 修改昵称
     */
    public void updateUsername(Long userId, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new BadRequestException("昵称不能为空");
        }
        if (newUsername.length() > 20) {
            throw new BadRequestException("昵称最长20位");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        user.setUsername(newUsername.trim());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 更新头像
     */
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 注销账号（软删除）
     */
    public void deleteAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        if ("ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole())) {
            throw new BadRequestException("管理员账号不能注销");
        }
        userMapper.deleteById(userId);
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(Long userId, String password) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 找回/重置密码
     */
    public void forgotPassword(String account, String email, String newPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 验证邮箱是否匹配
        if (email == null || !email.equals(user.getEmail())) {
            throw new BadRequestException("账号与邮箱不匹配");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);
    }

    // ==================== 管理员功能 ====================

    private boolean isSuperAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && "SUPER_ADMIN".equals(user.getRole());
    }

    private boolean isAnyAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && ("ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole()));
    }

    /**
     * 获取用户列表（管理员）
     */
    public List<UserVO> getUserList(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getAccount, keyword)
                   .or()
                   .like(User::getUsername, keyword)
                   .or()
                   .like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> pageParam = new Page<>(page, size);
        Page<User> userPage = userMapper.selectPage(pageParam, wrapper);

        return userPage.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户总数（管理员）
     */
    public long getUserCount(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getAccount, keyword)
                   .or()
                   .like(User::getUsername, keyword)
                   .or()
                   .like(User::getEmail, keyword);
        }
        return userMapper.selectCount(wrapper);
    }

    /**
     * 管理员删除用户
     */
    public void adminDeleteUser(Long adminId, Long targetUserId) {
        if (!isAnyAdmin(adminId)) {
            throw new UnauthorizedException("只有管理员可以执行此操作");
        }

        if (adminId.equals(targetUserId)) {
            throw new BadRequestException("不能删除自己的账号");
        }

        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BadRequestException("用户不存在");
        }

        // 只有超级管理员可以删除管理员
        if ("ADMIN".equals(targetUser.getRole()) && !isSuperAdmin(adminId)) {
            throw new BadRequestException("只有超级管理员可以删除管理员账号");
        }
        // 任何人都不能删除超级管理员
        if ("SUPER_ADMIN".equals(targetUser.getRole())) {
            throw new BadRequestException("不能删除超级管理员账号");
        }

        userMapper.deleteById(targetUserId);
    }

    /**
     * 管理员编辑用户
     */
    public void adminUpdateUser(Long adminId, Long targetUserId, String email, String role) {
        if (!isAnyAdmin(adminId)) {
            throw new UnauthorizedException("只有管理员可以执行此操作");
        }

        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BadRequestException("用户不存在");
        }

        // 普通管理员不能修改其他管理员和超级管理员
        if (!isSuperAdmin(adminId) && adminId.equals(targetUserId) == false
                && ("ADMIN".equals(targetUser.getRole()) || "SUPER_ADMIN".equals(targetUser.getRole()))) {
            throw new BadRequestException("只有超级管理员可以修改管理员信息");
        }

        // 不能修改超级管理员（即使是超管自己也不能降级，防止误操作）
        if ("SUPER_ADMIN".equals(targetUser.getRole()) && !adminId.equals(targetUserId)) {
            throw new BadRequestException("不能修改其他超级管理员的信息");
        }

        if (email != null && !email.isEmpty()) {
            targetUser.setEmail(email);
        }
        if (role != null && !role.isEmpty()) {
            if (!"USER".equals(role) && !"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
                throw new BadRequestException("无效的角色值，仅支持 USER、ADMIN 或 SUPER_ADMIN");
            }
            // 只有超级管理员才能设置超级管理员角色（但如果系统中还没有超管，允许管理员引导设置第一个）
            if ("SUPER_ADMIN".equals(role) && !isSuperAdmin(adminId)) {
                long superAdminCount = userMapper.selectCount(
                        new LambdaQueryWrapper<User>().eq(User::getRole, "SUPER_ADMIN"));
                if (superAdminCount > 0) {
                    throw new BadRequestException("只有超级管理员可以设置超级管理员角色");
                }
            }
            targetUser.setRole(role);
        }
        targetUser.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(targetUser);
    }

    /**
     * 管理员重置用户密码
     */
    public void adminResetPassword(Long adminId, Long targetUserId, String newPassword) {
        if (!isAnyAdmin(adminId)) {
            throw new UnauthorizedException("只有管理员可以执行此操作");
        }

        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BadRequestException("用户不存在");
        }

        // 普通管理员不能重置其他管理员的密码
        if (!isSuperAdmin(adminId) && !adminId.equals(targetUserId)
                && ("ADMIN".equals(targetUser.getRole()) || "SUPER_ADMIN".equals(targetUser.getRole()))) {
            throw new BadRequestException("只有超级管理员可以重置管理员密码");
        }

        targetUser.setPassword(passwordEncoder.encode(newPassword));
        targetUser.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(targetUser);
    }
}
