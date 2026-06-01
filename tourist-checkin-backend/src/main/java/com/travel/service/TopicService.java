package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.CheckInTopic;
import com.travel.entity.Topic;
import com.travel.mapper.CheckInTopicMapper;
import com.travel.mapper.TopicMapper;
import com.travel.vo.TopicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicMapper topicMapper;
    private final CheckInTopicMapper checkInTopicMapper;

    public List<TopicVO> trending() {
        return topicMapper.selectTrending().stream().map(this::convert).toList();
    }

    public List<TopicVO> search(String keyword) {
        return topicMapper.searchByKeyword(keyword).stream().map(this::convert).toList();
    }

    @Transactional
    public void attachToCheckIn(Long checkInId, List<Long> topicIds) {
        for (Long topicId : topicIds) {
            CheckInTopic ct = new CheckInTopic();
            ct.setCheckInId(checkInId);
            ct.setTopicId(topicId);
            checkInTopicMapper.insert(ct);

            Topic topic = topicMapper.selectById(topicId);
            if (topic != null) {
                topic.setCheckInCount(topic.getCheckInCount() + 1);
                topicMapper.updateById(topic);
            }
        }
    }

    @Transactional
    public Topic getOrCreateByName(String name) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getName, name);
        Topic topic = topicMapper.selectOne(wrapper);
        if (topic == null) {
            topic = new Topic();
            topic.setName(name);
            topic.setCheckInCount(0);
            topic.setViewCount(0);
            topic.setIsHot(0);
            topicMapper.insert(topic);
        }
        return topic;
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
