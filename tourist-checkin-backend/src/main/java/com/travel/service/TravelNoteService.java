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

    public TravelNoteVO detail(Long id, Long currentUserId) {
        TravelNote note = travelNoteMapper.selectById(id);
        if (note == null || note.getStatus() == 0) {
            throw new BadRequestException("游记不存在或已删除");
        }
        note.setViewCount(note.getViewCount() + 1);
        travelNoteMapper.updateById(note);
        return convertToVO(note, currentUserId);
    }

    public List<TravelNoteVO> listByUser(Long userId) {
        return travelNoteMapper.selectByUserId(userId).stream()
                .map(n -> convertToVO(n, userId)).toList();
    }

    public List<TravelNoteVO> listHot(int limit) {
        return travelNoteMapper.selectHot(limit).stream()
                .map(n -> convertToVO(n, null)).toList();
    }

    public List<TravelNoteVO> listRecent(int page, int size) {
        LambdaQueryWrapper<TravelNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNote::getStatus, 1)
               .orderByDesc(TravelNote::getCreatedAt);
        Page<TravelNote> p = new Page<>(page, size);
        return travelNoteMapper.selectPage(p, wrapper).getRecords().stream()
                .map(n -> convertToVO(n, null)).toList();
    }

    public List<TravelNoteVO> getLikedTravelNotes(Long userId) {
        return travelNoteMapper.selectLikedByUserId(userId).stream()
                .map(n -> convertToVO(n, userId)).toList();
    }

    @Transactional
    public void like(Long userId, Long noteId) {
        LambdaQueryWrapper<TravelNoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNoteLike::getNoteId, noteId)
               .eq(TravelNoteLike::getUserId, userId);
        if (travelNoteLikeMapper.selectCount(wrapper) > 0) {
            travelNoteLikeMapper.delete(wrapper);
            TravelNote note = travelNoteMapper.selectById(noteId);
            if (note != null && note.getLikeCount() > 0) {
                note.setLikeCount(note.getLikeCount() - 1);
                travelNoteMapper.updateById(note);
            }
        } else {
            try {
                TravelNoteLike like = new TravelNoteLike();
                like.setNoteId(noteId);
                like.setUserId(userId);
                travelNoteLikeMapper.insert(like);
                TravelNote note = travelNoteMapper.selectById(noteId);
                if (note != null) {
                    note.setLikeCount(note.getLikeCount() + 1);
                    travelNoteMapper.updateById(note);
                }
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
            TravelNote note = travelNoteMapper.selectById(noteId);
            if (note != null && note.getCollectCount() > 0) {
                note.setCollectCount(note.getCollectCount() - 1);
                travelNoteMapper.updateById(note);
            }
        } else {
            TravelNoteCollect col = new TravelNoteCollect();
            col.setNoteId(noteId);
            col.setUserId(userId);
            travelNoteCollectMapper.insert(col);
            TravelNote note = travelNoteMapper.selectById(noteId);
            if (note != null) {
                note.setCollectCount(note.getCollectCount() + 1);
                travelNoteMapper.updateById(note);
            }
        }
    }

    private TravelNoteVO convertToVO(TravelNote n, Long currentUserId) {
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
        vo.setHasLiked(currentUserId != null && checkLiked(currentUserId, n.getId()));
        vo.setHasCollected(currentUserId != null && checkCollected(currentUserId, n.getId()));
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());

        // 设置作者信息
        if (n.getUserId() != null) {
            User author = userMapper.selectById(n.getUserId());
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

    private boolean checkLiked(Long userId, Long noteId) {
        LambdaQueryWrapper<TravelNoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNoteLike::getNoteId, noteId)
               .eq(TravelNoteLike::getUserId, userId);
        return travelNoteLikeMapper.selectCount(wrapper) > 0;
    }

    private boolean checkCollected(Long userId, Long noteId) {
        LambdaQueryWrapper<TravelNoteCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNoteCollect::getNoteId, noteId)
               .eq(TravelNoteCollect::getUserId, userId);
        return travelNoteCollectMapper.selectCount(wrapper) > 0;
    }
}
