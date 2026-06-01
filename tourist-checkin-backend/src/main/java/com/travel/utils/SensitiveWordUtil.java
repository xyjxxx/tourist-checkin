package com.travel.utils;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.travel.entity.SensitiveWord;
import com.travel.mapper.SensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordUtil {

    private final SensitiveWordMapper sensitiveWordMapper;
    private SensitiveWordBs sensitiveWordBs;

    @PostConstruct
    public void init() {
        reloadWords();
    }

    public void reloadWords() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(null);
        List<String> wordList = words.stream()
                .filter(w -> w.getIsEnabled() != null && w.getIsEnabled() == 1)
                .map(SensitiveWord::getWord)
                .toList();

        sensitiveWordBs = SensitiveWordBs.newInstance()
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreNumStyle(true)
                .ignoreChineseStyle(true)
                .ignoreEnglishStyle(true)
                .enableWordCheck(true)
                .wordDeny(() -> wordList)
                .init();
        log.info("敏感词库加载完成: {} 个词", wordList.size());
    }

    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) return false;
        return sensitiveWordBs.contains(text);
    }

    public String replace(String text) {
        if (text == null || text.isEmpty()) return text;
        return sensitiveWordBs.replace(text);
    }
}
