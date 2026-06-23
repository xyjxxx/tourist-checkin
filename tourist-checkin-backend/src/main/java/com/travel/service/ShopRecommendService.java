package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.ShopRecommendCreateDTO;
import com.travel.entity.MerchantPosition;
import com.travel.entity.ShopRecommend;
import com.travel.entity.ShopRecommendLike;
import com.travel.entity.User;
import com.travel.exception.BadRequestException;
import com.travel.mapper.MerchantPositionMapper;
import com.travel.mapper.ShopRecommendLikeMapper;
import com.travel.mapper.ShopRecommendMapper;
import com.travel.mapper.UserMapper;
import com.travel.utils.PointUtil;
import com.travel.utils.SensitiveWordUtil;
import com.travel.vo.ShopRecommendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopRecommendService {

    private final ShopRecommendMapper shopRecommendMapper;
    private final ShopRecommendLikeMapper shopRecommendLikeMapper;
    private final UserMapper userMapper;
    private final MerchantPositionMapper merchantPositionMapper;
    private final SensitiveWordUtil sensitiveWordUtil;
    private final PointUtil pointUtil;
    private final ObjectMapper objectMapper;

    @Transactional
    public ShopRecommendVO submit(Long userId, ShopRecommendCreateDTO dto) {
        // Validate max 3 per day per user
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<ShopRecommend> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(ShopRecommend::getUserId, userId)
                .ge(ShopRecommend::getCreatedAt, todayStart);
        Long todayCount = shopRecommendMapper.selectCount(countWrapper);
        if (todayCount >= 3) {
            throw new BadRequestException("每天最多提交3条推荐");
        }

        // Validate no duplicate name+address for this user
        LambdaQueryWrapper<ShopRecommend> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(ShopRecommend::getUserId, userId)
                .eq(ShopRecommend::getName, dto.getName())
                .eq(ShopRecommend::getAddress, dto.getAddress());
        if (shopRecommendMapper.selectCount(dupWrapper) > 0) {
            throw new BadRequestException("您已推荐过该店铺");
        }

        // Sensitive word check on recommendReason
        if (sensitiveWordUtil.containsSensitive(dto.getRecommendReason())) {
            throw new BadRequestException("推荐理由包含敏感词，请修改后重试");
        }

        // Serialize images to JSON
        String imagesJson;
        try {
            imagesJson = objectMapper.writeValueAsString(dto.getImages());
        } catch (JsonProcessingException e) {
            throw new BadRequestException("图片数据格式错误");
        }

        ShopRecommend recommend = new ShopRecommend();
        recommend.setUserId(userId);
        recommend.setName(dto.getName());
        recommend.setCategory(dto.getCategory());
        recommend.setAddress(dto.getAddress());
        recommend.setLongitude(dto.getLongitude());
        recommend.setLatitude(dto.getLatitude());
        recommend.setCity(dto.getCity());
        recommend.setAvgPrice(dto.getAvgPrice());
        recommend.setImages(imagesJson);
        recommend.setRecommendReason(dto.getRecommendReason());
        recommend.setSignatureDish(dto.getSignatureDish());
        recommend.setBusinessHours(dto.getBusinessHours());
        recommend.setPhone(dto.getPhone());
        recommend.setWarning(dto.getWarning());
        recommend.setAuditStatus(0);
        recommend.setLikeCount(0);
        recommend.setCollectCount(0);
        recommend.setIsFeatured(0);
        shopRecommendMapper.insert(recommend);

        return convertToVO(recommend);
    }

    public Map<String, Object> mine(Long userId, int page, int size) {
        LambdaQueryWrapper<ShopRecommend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopRecommend::getUserId, userId)
                .orderByDesc(ShopRecommend::getCreatedAt);
        Page<ShopRecommend> p = new Page<>(page, size);
        Page<ShopRecommend> result = shopRecommendMapper.selectPage(p, wrapper);

        Map<Long, User> userMap = batchLoadUsers(result.getRecords().stream()
                .map(ShopRecommend::getUserId).toList());

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords().stream().map(r -> convertToVO(r, userMap)).toList());
        map.put("total", result.getTotal());
        return map;
    }

    public List<ShopRecommendVO> nearby(double lat, double lng, int radius,
                                         String category, int page, int size) {
        int offset = (page - 1) * size;
        List<Map<String, Object>> rows;

        if (category != null && !category.isEmpty()) {
            // SQL 级别分类过滤 + 分页
            rows = shopRecommendMapper.selectNearbyByCategory(lat, lng, radius, category, offset, size);
        } else {
            // 无分类过滤，使用 LIMIT offset, size
            int limit = offset + size;
            rows = shopRecommendMapper.selectNearby(lat, lng, radius, limit);
            if (rows.size() > offset) {
                rows = rows.subList(offset, Math.min(offset + size, rows.size()));
            } else {
                rows = List.of();
            }
        }

        List<Long> userIds = rows.stream()
                .map(row -> toLong(row.get("user_id")))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = batchLoadUsers(userIds);

        return rows.stream().map(row -> mapRowToVO(row, userMap)).toList();
    }

    public ShopRecommendVO detail(Long id) {
        ShopRecommend recommend = shopRecommendMapper.selectById(id);
        if (recommend == null) {
            throw new BadRequestException("推荐不存在");
        }
        return convertToVO(recommend);
    }

    @Transactional
    public void like(Long userId, Long id) {
        ShopRecommend recommend = shopRecommendMapper.selectById(id);
        if (recommend == null) {
            throw new BadRequestException("推荐不存在");
        }

        LambdaQueryWrapper<ShopRecommendLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopRecommendLike::getRecommendId, id)
                .eq(ShopRecommendLike::getUserId, userId);

        if (shopRecommendLikeMapper.selectCount(wrapper) > 0) {
            shopRecommendLikeMapper.delete(wrapper);
            shopRecommendMapper.decrementLikeCount(id);
        } else {
            try {
                ShopRecommendLike like = new ShopRecommendLike();
                like.setRecommendId(id);
                like.setUserId(userId);
                shopRecommendLikeMapper.insert(like);
                shopRecommendMapper.incrementLikeCount(id);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // Concurrent duplicate key, idempotent handling
            }
        }
    }

    @Transactional
    public ShopRecommendVO resubmit(Long userId, Long id, ShopRecommendCreateDTO dto) {
        ShopRecommend recommend = shopRecommendMapper.selectById(id);
        if (recommend == null) {
            throw new BadRequestException("推荐不存在");
        }
        if (!recommend.getUserId().equals(userId)) {
            throw new BadRequestException("无权操作");
        }
        if (recommend.getAuditStatus() != -1) {
            throw new BadRequestException("只有被驳回的推荐才能重新提交");
        }

        // Sensitive word check
        if (sensitiveWordUtil.containsSensitive(dto.getRecommendReason())) {
            throw new BadRequestException("推荐理由包含敏感词，请修改后重试");
        }

        String imagesJson;
        try {
            imagesJson = objectMapper.writeValueAsString(dto.getImages());
        } catch (JsonProcessingException e) {
            throw new BadRequestException("图片数据格式错误");
        }

        recommend.setName(dto.getName());
        recommend.setCategory(dto.getCategory());
        recommend.setAddress(dto.getAddress());
        recommend.setLongitude(dto.getLongitude());
        recommend.setLatitude(dto.getLatitude());
        recommend.setCity(dto.getCity());
        recommend.setAvgPrice(dto.getAvgPrice());
        recommend.setImages(imagesJson);
        recommend.setRecommendReason(dto.getRecommendReason());
        recommend.setSignatureDish(dto.getSignatureDish());
        recommend.setBusinessHours(dto.getBusinessHours());
        recommend.setPhone(dto.getPhone());
        recommend.setWarning(dto.getWarning());
        recommend.setAuditStatus(0);
        recommend.setAuditReason(null);
        recommend.setAuditorId(null);
        recommend.setAuditTime(null);
        shopRecommendMapper.updateById(recommend);

        return convertToVO(recommend);
    }

    // ==================== Admin ====================

    public Map<String, Object> adminList(int page, int size, Integer status, String keyword) {
        LambdaQueryWrapper<ShopRecommend> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ShopRecommend::getAuditStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ShopRecommend::getName, keyword)
                    .or().like(ShopRecommend::getAddress, keyword));
        }
        wrapper.orderByDesc(ShopRecommend::getCreatedAt);
        Page<ShopRecommend> p = new Page<>(page, size);
        Page<ShopRecommend> result = shopRecommendMapper.selectPage(p, wrapper);

        Map<Long, User> userMap = batchLoadUsers(result.getRecords().stream()
                .map(ShopRecommend::getUserId).toList());

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords().stream().map(r -> convertToVO(r, userMap)).toList());
        map.put("total", result.getTotal());
        return map;
    }

    @Transactional
    public void adminAudit(Long id, Long auditorId, int status, String reason) {
        ShopRecommend recommend = shopRecommendMapper.selectById(id);
        if (recommend == null) {
            throw new BadRequestException("推荐不存在");
        }

        recommend.setAuditorId(auditorId);
        recommend.setAuditTime(LocalDateTime.now());

        if (status == 1) {
            // Approve: create MerchantPosition
            MerchantPosition position = new MerchantPosition();
            position.setName(recommend.getName());
            position.setCategory(recommend.getCategory());
            position.setAddress(recommend.getAddress());
            position.setLongitude(recommend.getLongitude());
            position.setLatitude(recommend.getLatitude());
            position.setCity(recommend.getCity());
            position.setPhone(recommend.getPhone());
            position.setBusinessHours(recommend.getBusinessHours());
            position.setCoverImage(parseFirstImage(recommend.getImages()));
            position.setDescription(recommend.getRecommendReason());
            position.setStatus(1);
            merchantPositionMapper.insert(position);

            recommend.setMerchantId(position.getId());
            recommend.setAuditStatus(1);
            recommend.setAuditReason(null);

            // Award points to recommender
            pointUtil.addPoints(recommend.getUserId(), "SHOP_RECOMMEND", 10,
                    "推荐店铺通过审核", "SHOP_RECOMMEND", recommend.getId());
        } else if (status == -1) {
            // Reject
            recommend.setAuditStatus(-1);
            recommend.setAuditReason(reason);
        } else {
            throw new BadRequestException("无效的审核状态");
        }

        shopRecommendMapper.updateById(recommend);
    }

    public void adminFeature(Long id, int toggle) {
        ShopRecommend recommend = shopRecommendMapper.selectById(id);
        if (recommend == null) {
            throw new BadRequestException("推荐不存在");
        }
        recommend.setIsFeatured(toggle);
        shopRecommendMapper.updateById(recommend);
    }

    public Map<String, Object> adminStats() {
        long totalPending = shopRecommendMapper.selectCount(
                new LambdaQueryWrapper<ShopRecommend>().eq(ShopRecommend::getAuditStatus, 0));
        long totalApproved = shopRecommendMapper.selectCount(
                new LambdaQueryWrapper<ShopRecommend>().eq(ShopRecommend::getAuditStatus, 1));
        long totalRejected = shopRecommendMapper.selectCount(
                new LambdaQueryWrapper<ShopRecommend>().eq(ShopRecommend::getAuditStatus, -1));

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long weekCount = shopRecommendMapper.selectCount(
                new LambdaQueryWrapper<ShopRecommend>().ge(ShopRecommend::getCreatedAt, weekAgo));

        long featuredCount = shopRecommendMapper.selectCount(
                new LambdaQueryWrapper<ShopRecommend>().eq(ShopRecommend::getIsFeatured, 1));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPending", totalPending);
        stats.put("totalApproved", totalApproved);
        stats.put("totalRejected", totalRejected);
        stats.put("weekCount", weekCount);
        stats.put("featuredCount", featuredCount);
        return stats;
    }

    // ==================== Converters ====================

    private Map<Long, User> batchLoadUsers(List<Long> userIds) {
        List<Long> uniqueIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (uniqueIds.isEmpty()) return Map.of();
        List<User> users = userMapper.selectBatchIds(uniqueIds);
        Map<Long, User> map = new HashMap<>(users.size());
        for (User u : users) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private ShopRecommendVO convertToVO(ShopRecommend r) {
        Map<Long, User> userMap = r.getUserId() != null
                ? batchLoadUsers(List.of(r.getUserId())) : Map.of();
        return convertToVO(r, userMap);
    }

    private ShopRecommendVO convertToVO(ShopRecommend r, Map<Long, User> userMap) {
        ShopRecommendVO vo = new ShopRecommendVO();
        vo.setId(r.getId());
        vo.setUserId(r.getUserId());
        vo.setName(r.getName());
        vo.setCategory(r.getCategory());
        vo.setAddress(r.getAddress());
        vo.setLongitude(r.getLongitude());
        vo.setLatitude(r.getLatitude());
        vo.setCity(r.getCity());
        vo.setAvgPrice(r.getAvgPrice());
        vo.setImages(parseImages(r.getImages()));
        vo.setRecommendReason(r.getRecommendReason());
        vo.setSignatureDish(r.getSignatureDish());
        vo.setBusinessHours(r.getBusinessHours());
        vo.setPhone(r.getPhone());
        vo.setWarning(r.getWarning());
        vo.setAuditStatus(r.getAuditStatus());
        vo.setAuditReason(r.getAuditReason());
        vo.setMerchantId(r.getMerchantId());
        vo.setLikeCount(r.getLikeCount());
        vo.setCollectCount(r.getCollectCount());
        vo.setIsFeatured(r.getIsFeatured());
        vo.setCreatedAt(r.getCreatedAt());

        // Look up user info from preloaded map
        if (r.getUserId() != null) {
            User user = userMap.get(r.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }
        }

        return vo;
    }

    private ShopRecommendVO mapRowToVO(Map<String, Object> row, Map<Long, User> userMap) {
        ShopRecommendVO vo = new ShopRecommendVO();
        vo.setId(toLong(row.get("id")));
        vo.setUserId(toLong(row.get("user_id")));
        vo.setName((String) row.get("name"));
        vo.setCategory((String) row.get("category"));
        vo.setAddress((String) row.get("address"));
        vo.setLongitude(toBigDecimal(row.get("longitude")));
        vo.setLatitude(toBigDecimal(row.get("latitude")));
        vo.setCity((String) row.get("city"));
        vo.setAvgPrice(toInteger(row.get("avg_price")));
        vo.setImages(parseImages((String) row.get("images")));
        vo.setRecommendReason((String) row.get("recommend_reason"));
        vo.setSignatureDish((String) row.get("signature_dish"));
        vo.setBusinessHours((String) row.get("business_hours"));
        vo.setPhone((String) row.get("phone"));
        vo.setWarning((String) row.get("warning"));
        vo.setAuditStatus(toInteger(row.get("audit_status")));
        vo.setAuditReason((String) row.get("audit_reason"));
        vo.setMerchantId(toLong(row.get("merchant_id")));
        vo.setLikeCount(toInteger(row.get("like_count")));
        vo.setCollectCount(toInteger(row.get("collect_count")));
        vo.setIsFeatured(toInteger(row.get("is_featured")));
        vo.setDistance(toDouble(row.get("distance")));

        // Look up user info from preloaded map
        if (vo.getUserId() != null) {
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }
        }

        return vo;
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(imagesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse images JSON: {}", imagesJson, e);
            return List.of();
        }
    }

    private String parseFirstImage(String imagesJson) {
        List<String> images = parseImages(imagesJson);
        return images.isEmpty() ? null : images.get(0);
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        return Long.valueOf(val.toString());
    }

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        return Integer.valueOf(val.toString());
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Double) return (Double) val;
        return Double.valueOf(val.toString());
    }

    private java.math.BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof java.math.BigDecimal) return (java.math.BigDecimal) val;
        return new java.math.BigDecimal(val.toString());
    }
}
