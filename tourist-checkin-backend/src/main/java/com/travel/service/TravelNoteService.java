package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.TravelNoteCreateDTO;
import com.travel.entity.TravelNote;
import com.travel.entity.TravelNoteCollect;
import com.travel.entity.TravelNoteImage;
import com.travel.entity.TravelNoteLike;
import com.travel.exception.BadRequestException;
import com.travel.mapper.TravelNoteCollectMapper;
import com.travel.mapper.TravelNoteImageMapper;
import com.travel.mapper.TravelNoteLikeMapper;
import com.travel.mapper.TravelNoteMapper;
import com.travel.mapper.UserMapper;
import com.travel.entity.User;
import com.travel.utils.PointUtil;
import com.travel.utils.SensitiveWordUtil;
import com.travel.vo.TravelNoteVO;
import com.travel.vo.UserBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelNoteService {

    private final TravelNoteMapper travelNoteMapper;
    private final TravelNoteImageMapper travelNoteImageMapper;
    private final TravelNoteLikeMapper travelNoteLikeMapper;
    private final TravelNoteCollectMapper travelNoteCollectMapper;
    private final UserMapper userMapper;
    private final SensitiveWordUtil sensitiveWordUtil;
    private final PointUtil pointUtil;

    @Transactional
    public TravelNoteVO create(Long userId, TravelNoteCreateDTO dto) {
        String cleanContent = sensitiveWordUtil.replace(dto.getContent());
        String cleanTitle = sensitiveWordUtil.replace(dto.getTitle());

        TravelNote note = new TravelNote();
        note.setUserId(userId);
        note.setTitle(cleanTitle);
        note.setSummary(cleanContent.length() > 200 ? cleanContent.substring(0, 200) : cleanContent);
        note.setContent(cleanContent);
        note.setCoverImage(dto.getCoverImage());
        note.setCity(dto.getCity());
        List<String> tags = dto.getTags();
        note.setTags(tags != null && !tags.isEmpty() ? com.alibaba.fastjson2.JSON.toJSONString(tags) : null);
        note.setStatus(1);
        note.setViewCount(0);
        note.setLikeCount(0);
        note.setCollectCount(0);
        note.setCommentCount(0);
        note.setIsPinned(0);
        travelNoteMapper.insert(note);

        if (dto.getImages() != null) {
            int sort = 0;
            for (String url : dto.getImages()) {
                TravelNoteImage img = new TravelNoteImage();
                img.setNoteId(note.getId());
                img.setUrl(url);
                img.setSortOrder(sort++);
                travelNoteImageMapper.insert(img);
            }
        }

        pointUtil.addPoints(userId, "TRAVEL_NOTE", PointUtil.POINTS_CHECK_IN * 2,
                "发布游记", "TRAVEL_NOTE", note.getId());

        return convertToVO(note, userId);
    }

    @Transactional
    public TravelNoteVO detail(Long id, Long currentUserId) {
        TravelNote note = travelNoteMapper.selectById(id);
        if (note == null || note.getStatus() == 0) {
            throw new BadRequestException("游记不存在或已删除");
        }
        travelNoteMapper.incrementViewCount(id);
        note.setViewCount(note.getViewCount() + 1);
        return convertToVO(note, currentUserId);
    }

    public List<TravelNoteVO> listByUser(Long userId) {
        return convertToVOList(travelNoteMapper.selectByUserId(userId), userId);
    }

    public List<TravelNoteVO> listHot(int limit) {
        return convertToVOList(travelNoteMapper.selectHot(limit), null);
    }

    public List<TravelNoteVO> listRecent(int page, int size) {
        return listRecent(page, size, null);
    }

    public List<TravelNoteVO> listRecent(int page, int size, String keyword) {
        LambdaQueryWrapper<TravelNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNote::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TravelNote::getTitle, keyword)
                    .or().like(TravelNote::getContent, keyword)
                    .or().like(TravelNote::getCity, keyword));
        }
        wrapper.orderByDesc(TravelNote::getCreatedAt);
        Page<TravelNote> p = new Page<>(page, size);
        return convertToVOList(travelNoteMapper.selectPage(p, wrapper).getRecords(), null);
    }

    public List<TravelNoteVO> getLikedTravelNotes(Long userId) {
        return convertToVOList(travelNoteMapper.selectLikedByUserId(userId), userId);
    }

    public List<TravelNoteVO> getCollectedTravelNotes(Long userId) {
        return convertToVOList(travelNoteMapper.selectCollectedByUserId(userId), userId);
    }

    @Transactional
    public void like(Long userId, Long noteId) {
        LambdaQueryWrapper<TravelNoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNoteLike::getNoteId, noteId)
               .eq(TravelNoteLike::getUserId, userId);
        int deleted = travelNoteLikeMapper.delete(wrapper);
        if (deleted > 0) {
            travelNoteMapper.decrementLikeCount(noteId);
        } else {
            try {
                TravelNoteLike like = new TravelNoteLike();
                like.setNoteId(noteId);
                like.setUserId(userId);
                travelNoteLikeMapper.insert(like);
                travelNoteMapper.incrementLikeCount(noteId);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发场景下唯一索引冲突：其他线程已点赞，幂等处理
            }
        }
    }

    @Transactional
    public void collect(Long userId, Long noteId) {
        LambdaQueryWrapper<TravelNoteCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNoteCollect::getNoteId, noteId)
               .eq(TravelNoteCollect::getUserId, userId);
        if (travelNoteCollectMapper.selectCount(wrapper) > 0) {
            travelNoteCollectMapper.delete(wrapper);
            travelNoteMapper.decrementCollectCount(noteId);
        } else {
            try {
                TravelNoteCollect col = new TravelNoteCollect();
                col.setNoteId(noteId);
                col.setUserId(userId);
                travelNoteCollectMapper.insert(col);
                travelNoteMapper.incrementCollectCount(noteId);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发场景下唯一索引冲突：其他线程已收藏，幂等处理
            }
        }
    }

    private TravelNoteVO convertToVO(TravelNote n, Long currentUserId) {
        TravelNoteVO vo = convertToVO(n, currentUserId, Map.of(), Set.of(), Set.of());
        // 加载游记图片
        List<TravelNoteImage> images = travelNoteImageMapper.selectList(
                new LambdaQueryWrapper<TravelNoteImage>()
                        .eq(TravelNoteImage::getNoteId, n.getId())
                        .orderByAsc(TravelNoteImage::getSortOrder));
        vo.setImages(images.stream().map(TravelNoteImage::getUrl).toList());
        return vo;
    }

    private TravelNoteVO convertToVO(TravelNote n, Long currentUserId,
                                      Map<Long, User> userMap,
                                      Set<Long> likedNoteIds,
                                      Set<Long> collectedNoteIds) {
        TravelNoteVO vo = new TravelNoteVO();
        vo.setId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setSummary(n.getSummary());
        vo.setCoverImage(n.getCoverImage());
        vo.setContent(n.getContent());
        vo.setCity(n.getCity());
        vo.setTags(n.getTags() != null && !n.getTags().isEmpty()
                ? com.alibaba.fastjson2.JSON.parseArray(n.getTags(), String.class) : List.of());
        vo.setViewCount(n.getViewCount());
        vo.setLikeCount(n.getLikeCount());
        vo.setCollectCount(n.getCollectCount());
        vo.setCommentCount(n.getCommentCount());
        vo.setHasLiked(likedNoteIds.contains(n.getId()));
        vo.setHasCollected(collectedNoteIds.contains(n.getId()));
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());

        if (n.getUserId() != null) {
            User author = userMap.get(n.getUserId());
            if (author != null) {
                UserBriefVO authorVO = new UserBriefVO();
                authorVO.setId(author.getId());
                authorVO.setUsername(author.getUsername());
                authorVO.setAvatar(author.getAvatar());
                vo.setAuthor(authorVO);
            }
        }

        return vo;
    }

    private List<TravelNoteVO> convertToVOList(List<TravelNote> notes, Long currentUserId) {
        if (notes.isEmpty()) return List.of();

        // 批量加载作者信息
        Set<Long> userIds = notes.stream().map(TravelNote::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量加载点赞/收藏状态
        Set<Long> likedNoteIds = Set.of();
        Set<Long> collectedNoteIds = Set.of();
        if (currentUserId != null) {
            List<Long> noteIds = notes.stream().map(TravelNote::getId).toList();
            likedNoteIds = travelNoteLikeMapper.selectList(
                    new LambdaQueryWrapper<TravelNoteLike>()
                            .eq(TravelNoteLike::getUserId, currentUserId)
                            .in(TravelNoteLike::getNoteId, noteIds))
                    .stream().map(TravelNoteLike::getNoteId).collect(Collectors.toSet());
            collectedNoteIds = travelNoteCollectMapper.selectList(
                    new LambdaQueryWrapper<TravelNoteCollect>()
                            .eq(TravelNoteCollect::getUserId, currentUserId)
                            .in(TravelNoteCollect::getNoteId, noteIds))
                    .stream().map(TravelNoteCollect::getNoteId).collect(Collectors.toSet());
        }

        // 批量加载游记图片
        List<Long> noteIds = notes.stream().map(TravelNote::getId).toList();
        Map<Long, List<String>> imagesMap = travelNoteImageMapper.selectList(
                new LambdaQueryWrapper<TravelNoteImage>()
                        .in(TravelNoteImage::getNoteId, noteIds)
                        .orderByAsc(TravelNoteImage::getSortOrder))
                .stream().collect(Collectors.groupingBy(
                        TravelNoteImage::getNoteId,
                        Collectors.mapping(TravelNoteImage::getUrl, Collectors.toList())));

        Map<Long, User> finalUserMap = userMap;
        Set<Long> finalLikedNoteIds = likedNoteIds;
        Set<Long> finalCollectedNoteIds = collectedNoteIds;
        return notes.stream()
                .map(n -> {
                    TravelNoteVO vo = convertToVO(n, currentUserId, finalUserMap, finalLikedNoteIds, finalCollectedNoteIds);
                    vo.setImages(imagesMap.getOrDefault(n.getId(), List.of()));
                    return vo;
                })
                .toList();
    }

    // ==================== 管理员功能 ====================

    public Map<String, Object> adminList(int page, int size, Integer status, String keyword) {
        LambdaQueryWrapper<TravelNote> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TravelNote::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TravelNote::getTitle, keyword).or().like(TravelNote::getCity, keyword));
        }
        wrapper.orderByDesc(TravelNote::getCreatedAt);
        Page<TravelNote> p = new Page<>(page, size);
        Page<TravelNote> result = travelNoteMapper.selectPage(p, wrapper);

        Map<String, Object> map = new java.util.HashMap<>();
        map.put("list", result.getRecords().stream().map(n -> convertToVO(n, null)).toList());
        map.put("total", result.getTotal());
        return map;
    }

    @Transactional
    public void adminAudit(Long id, int status) {
        TravelNote note = travelNoteMapper.selectById(id);
        if (note == null) throw new BadRequestException("游记不存在");
        note.setStatus(status);
        travelNoteMapper.updateById(note);
    }

    @Transactional
    public void adminDelete(Long id) {
        TravelNote note = travelNoteMapper.selectById(id);
        if (note == null) throw new BadRequestException("游记不存在");
        note.setStatus(0);
        travelNoteMapper.updateById(note);
    }

    @Transactional
    public void adminTogglePin(Long id) {
        TravelNote note = travelNoteMapper.selectById(id);
        if (note == null) throw new BadRequestException("游记不存在");
        note.setIsPinned(note.getIsPinned() == 1 ? 0 : 1);
        travelNoteMapper.updateById(note);
    }
}
