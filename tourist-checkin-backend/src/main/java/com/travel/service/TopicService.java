package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.CheckIn;
import com.travel.entity.CheckInTopic;
import com.travel.entity.Topic;
import com.travel.mapper.CheckInMapper;
import com.travel.mapper.CheckInTopicMapper;
import com.travel.mapper.TopicMapper;
import com.travel.vo.TopicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicMapper topicMapper;
    private final CheckInTopicMapper checkInTopicMapper;
    private final CheckInMapper checkInMapper;

    public List<TopicVO> trending() {
        return topicMapper.selectTrending().stream().map(this::convert).toList();
    }

    public List<TopicVO> search(String keyword) {
        return topicMapper.searchByKeyword(keyword).stream().map(this::convert).toList();
    }

    @Transactional
    public void attachToCheckIn(Long userId, Long checkInId, List<Long> topicIds) {
        // 验证打卡归属权
        CheckIn checkIn = checkInMapper.selectById(checkInId);
        if (checkIn == null || !checkIn.getUserId().equals(userId)) {
            throw new com.travel.exception.UnauthorizedException("无权操作此打卡");
        }
        for (Long topicId : topicIds) {
            // 防止重复关联
            Long count = checkInTopicMapper.selectCount(
                    new LambdaQueryWrapper<CheckInTopic>()
                            .eq(CheckInTopic::getCheckInId, checkInId)
                            .eq(CheckInTopic::getTopicId, topicId));
            if (count > 0) {
                continue;
            }

            CheckInTopic ct = new CheckInTopic();
            ct.setCheckInId(checkInId);
            ct.setTopicId(topicId);
            checkInTopicMapper.insert(ct);

            topicMapper.incrementCheckInCount(topicId);
        }
    }

    @Transactional
    public Topic getOrCreateByName(String name) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getName, name);
        Topic topic = topicMapper.selectOne(wrapper);
        if (topic == null) {
            try {
                topic = new Topic();
                topic.setName(name);
                topic.setCheckInCount(0);
                topic.setViewCount(0);
                topic.setIsHot(0);
                topicMapper.insert(topic);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                topic = topicMapper.selectOne(wrapper);
            }
        }
        return topic;
    }

    // ==================== 管理员功能 ====================

    public Map<String, Object> adminList(int page, int size, String keyword) {
        if (page < 1) page = 1;
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Topic::getName, keyword);
        }
        wrapper.orderByDesc(Topic::getCreatedAt);
        Page<Topic> p = new Page<>(page, size);
        Page<Topic> result = topicMapper.selectPage(p, wrapper);
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("list", result.getRecords().stream().map(this::convert).toList());
        map.put("total", result.getTotal());
        return map;
    }

    public TopicVO adminCreate(String name, String icon, String description) {
        Topic topic = new Topic();
        topic.setName(name);
        topic.setIcon(icon);
        topic.setDescription(description);
        topic.setCheckInCount(0);
        topic.setViewCount(0);
        topic.setIsHot(0);
        topicMapper.insert(topic);
        return convert(topic);
    }

    public void adminUpdate(Long id, String name, String icon, String description, Integer isHot) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) throw new com.travel.exception.BadRequestException("话题不存在");
        if (name != null) topic.setName(name);
        if (icon != null) topic.setIcon(icon);
        if (description != null) topic.setDescription(description);
        if (isHot != null) topic.setIsHot(isHot);
        topicMapper.updateById(topic);
    }

    @Transactional
    public void adminDelete(Long id) {
        // 先删除关联的 check_in_topic 记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.travel.entity.CheckInTopic> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.travel.entity.CheckInTopic::getTopicId, id);
        checkInTopicMapper.delete(wrapper);
        topicMapper.deleteById(id);
    }

    private TopicVO convert(Topic t) {
        TopicVO vo = new TopicVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setIcon(t.getIcon());
        vo.setDescription(t.getDescription());
        vo.setCheckInCount(t.getCheckInCount());
        vo.setViewCount(t.getViewCount());
        vo.setIsHot(t.getIsHot() == 1);
        return vo;
    }
}
