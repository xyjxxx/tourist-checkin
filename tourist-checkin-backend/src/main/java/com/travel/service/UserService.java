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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final com.travel.mapper.FollowMapper followMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wx.appid:}")
    private String wxAppId;

    @Value("${wx.secret:}")
    private String wxSecret;

    public String register(UserRegisterDTO dto) {
        // 检查账号是否已被占用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, dto.getAccount());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BadRequestException("账号已存在");
        }

        // 检查邮箱是否已被占用
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, dto.getEmail());
            if (userMapper.selectCount(emailWrapper) > 0) {
                throw new BadRequestException("该邮箱已被注册");
            }
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

        // 账号未找到，尝试用邮箱登录
        if (user == null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, dto.getAccount());
            user = userMapper.selectOne(wrapper);
        }

        if (user == null) {
            throw new UnauthorizedException("账号或密码错误");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new UnauthorizedException("该账号为微信登录用户，请使用微信登录");
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

    @SuppressWarnings("unchecked")
    public String wxLogin(String code) {
        if (wxAppId == null || wxAppId.isEmpty() || wxSecret == null || wxSecret.isEmpty()) {
            throw new BadRequestException("微信小程序未配置");
        }

        String url = org.springframework.web.util.UriComponentsBuilder
                .fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", wxAppId)
                .queryParam("secret", wxSecret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUriString();

        Map<String, Object> wxResponse;
        try {
            wxResponse = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("调用微信接口失败", e);
            throw new BadRequestException("微信登录服务异常，请稍后重试");
        }

        if (wxResponse == null) {
            throw new BadRequestException("微信接口无响应");
        }
        if (wxResponse.containsKey("errcode") && !Integer.valueOf(0).equals(wxResponse.get("errcode"))) {
            log.warn("微信登录失败: errcode={}, errmsg={}", wxResponse.get("errcode"), wxResponse.get("errmsg"));
            throw new BadRequestException("微信登录失败: " + wxResponse.get("errmsg"));
        }

        String openid = (String) wxResponse.get("openid");
        if (openid == null || openid.isEmpty()) {
            throw new BadRequestException("获取openid失败");
        }

        // 查找或创建用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUsername("微信用户");
            user.setRole("USER");
            userMapper.insert(user);
            log.info("微信新用户注册: id={}", user.getId());
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        fillUserStats(userId, vo);
        return vo;
    }

    /**
     * 获取用户公开资料（隐藏敏感信息）
     */
    public UserVO getUserProfile(Long userId, Long currentUserId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setEmail(null); // 隐藏邮箱
        fillUserStats(userId, vo);
        // 查询关注状态
        if (currentUserId != null && !currentUserId.equals(userId)) {
            vo.setIsFollowing(followMapper.isFollowing(currentUserId, userId));
        } else {
            vo.setIsFollowing(false);
        }
        return vo;
    }

    private void fillUserStats(Long userId, UserVO vo) {
        java.util.Map<String, Object> stats = userMapper.selectUserStats(userId);
        if (stats != null) {
            vo.setCheckinCount(toInt(stats.get("checkinCount")));
            vo.setFollowerCount(toInt(stats.get("followerCount")));
            vo.setFollowingCount(toInt(stats.get("followingCount")));
            vo.setPoints(toInt(stats.get("points")));
            vo.setLevel(toInt(stats.get("level")));
        } else {
            vo.setCheckinCount(0);
            vo.setFollowerCount(0);
            vo.setFollowingCount(0);
            vo.setPoints(0);
            vo.setLevel(0);
        }
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    /**
     * 更新背景图
     */
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
        // 微信用户无密码，无法通过密码验证
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 找回/重置密码
     */
    public boolean forgotPassword(String account, String email, String newPassword) {
        log.warn("密码重置请求: account={}", account);

        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("密码至少6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BadRequestException("账号不存在或邮箱不匹配");
        }

        // 验证邮箱是否匹配
        if (email == null || !email.equals(user.getEmail())) {
            throw new BadRequestException("账号不存在或邮箱不匹配");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.updateById(user);
        return true;
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
    public List<UserVO> getUserList(int page, int size, String keyword, String loginType, String role) {
        LambdaQueryWrapper<User> wrapper = buildUserQueryWrapper(keyword, loginType, role);
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
    public long getUserCount(String keyword, String loginType, String role) {
        LambdaQueryWrapper<User> wrapper = buildUserQueryWrapper(keyword, loginType, role);
        return userMapper.selectCount(wrapper);
    }

    private LambdaQueryWrapper<User> buildUserQueryWrapper(String keyword, String loginType, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getAccount, keyword)
                    .or()
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if ("wechat".equals(loginType)) {
            wrapper.isNotNull(User::getOpenid).ne(User::getOpenid, "");
        } else if ("account".equals(loginType)) {
            wrapper.and(w -> w.isNull(User::getOpenid).or().eq(User::getOpenid, ""));
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        return wrapper;
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
    @Transactional
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
    @Transactional
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
