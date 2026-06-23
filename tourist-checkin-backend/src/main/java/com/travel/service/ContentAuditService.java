package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.ReportCreateDTO;
import com.travel.entity.ImageAudit;
import com.travel.entity.Report;
import com.travel.entity.SensitiveWord;
import com.travel.mapper.ImageAuditMapper;
import com.travel.mapper.ReportMapper;
import com.travel.mapper.SensitiveWordMapper;
import com.travel.utils.SensitiveWordUtil;
import com.travel.vo.ImageAuditVO;
import com.travel.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentAuditService {

    private final ReportMapper reportMapper;
    private final ImageAuditMapper imageAuditMapper;
    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordUtil sensitiveWordUtil;

    @Transactional
    public void submitReport(Long reporterId, ReportCreateDTO dto) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setReportedUserId(dto.getReportedUserId());
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setDetail(dto.getDetail());
        report.setStatus(0);
        reportMapper.insert(report);
    }

    public Map<String, Object> listReports(int status, int page, int size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status >= 0) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        Page<Report> p = new Page<>(page, size);
        Page<Report> result = reportMapper.selectPage(p, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords().stream().map(r -> {
            ReportVO vo = new ReportVO();
            vo.setId(r.getId());
            vo.setTargetType(r.getTargetType());
            vo.setTargetId(r.getTargetId());
            vo.setReason(r.getReason());
            vo.setDetail(r.getDetail());
            vo.setStatus(r.getStatus());
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).toList());
        map.put("total", result.getTotal());
        return map;
    }

    @Transactional
    public void handleReport(Long handlerId, Long reportId, int status, String result) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new com.travel.exception.BadRequestException("举报记录不存在");
        }
        report.setStatus(status);
        report.setHandlerId(handlerId);
        report.setHandleResult(result);
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    public Map<String, Object> listImageAudits(int status, int page, int size) {
        LambdaQueryWrapper<ImageAudit> wrapper = new LambdaQueryWrapper<>();
        if (status >= 0) {
            wrapper.eq(ImageAudit::getAuditStatus, status);
        }
        wrapper.orderByDesc(ImageAudit::getCreatedAt);
        Page<ImageAudit> p = new Page<>(page, size);
        Page<ImageAudit> result = imageAuditMapper.selectPage(p, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords().stream().map(a -> {
            ImageAuditVO vo = new ImageAuditVO();
            vo.setId(a.getId());
            vo.setImageUrl(a.getImageUrl());
            vo.setSourceType(a.getSourceType());
            vo.setSourceId(a.getSourceId());
            vo.setAuditStatus(a.getAuditStatus());
            vo.setAuditResult(a.getAuditResult());
            vo.setCreatedAt(a.getCreatedAt());
            return vo;
        }).toList());
        map.put("total", result.getTotal());
        return map;
    }

    @Transactional
    public void auditImage(Long auditorId, Long imageId, int status, String result) {
        ImageAudit audit = imageAuditMapper.selectById(imageId);
        if (audit == null) {
            throw new com.travel.exception.BadRequestException("审核记录不存在");
        }
        audit.setAuditStatus(status);
        audit.setAuditResult(result);
        audit.setAuditorId(auditorId);
        audit.setAuditTime(LocalDateTime.now());
        imageAuditMapper.updateById(audit);
    }

    public Map<String, Object> listSensitiveWords(int page, int size) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getIsEnabled, 1);
        Page<SensitiveWord> p = new Page<>(page, size);
        Page<SensitiveWord> result = sensitiveWordMapper.selectPage(p, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords().stream().map(SensitiveWord::getWord).toList());
        map.put("total", result.getTotal());
        return map;
    }

    @Transactional
    public void addSensitiveWord(String word, String category, int level) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getWord, word);
        Long count = sensitiveWordMapper.selectCount(wrapper);
        if (count > 0) {
            throw new com.travel.exception.BadRequestException("敏感词已存在");
        }
        SensitiveWord sw = new SensitiveWord();
        sw.setWord(word);
        sw.setCategory(category);
        sw.setLevel(level);
        sw.setIsEnabled(1);
        sensitiveWordMapper.insert(sw);
        sensitiveWordUtil.reloadWords();
    }

    @Transactional
    public void removeSensitiveWord(Long id) {
        SensitiveWord sw = sensitiveWordMapper.selectById(id);
        if (sw != null) {
            sw.setIsEnabled(0);
            sensitiveWordMapper.updateById(sw);
            sensitiveWordUtil.reloadWords();
        }
    }
}
