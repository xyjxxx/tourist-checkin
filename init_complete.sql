-- ============================================================
-- 拾光旅记 (Tourist Check-in) 完整数据库初始化脚本
-- 合并自: init.sql + migration_v2.sql + migration_v3_fixes.sql + migration_v4_account.sql + fix_test_user.sql
-- 数据库: tourist_checkin
-- 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS tourist_checkin
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE tourist_checkin;

-- ============================================================
-- 模块 1: 用户系统
-- ============================================================
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `account` VARCHAR(50) NOT NULL COMMENT '登录账号(唯一)',
    `username` VARCHAR(50) NOT NULL COMMENT '用户昵称',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `background_image` VARCHAR(500) COMMENT '背景图URL',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER/ADMIN/SUPER_ADMIN',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE KEY `uk_account` (`account`),
    UNIQUE KEY `uk_email` (`email`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 模块 2: 地点系统
-- ============================================================
CREATE TABLE `location` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地点ID',
    `name` VARCHAR(100) NOT NULL COMMENT '地点名称',
    `address` VARCHAR(255) COMMENT '详细地址',
    `longitude` DECIMAL(10, 7) NOT NULL COMMENT '经度',
    `latitude` DECIMAL(10, 7) NOT NULL COMMENT '纬度',
    `category` VARCHAR(50) COMMENT '分类: 景点/餐厅/酒店/其他',
    `city` VARCHAR(50) COMMENT '城市',
    `description` TEXT COMMENT '地点介绍',
    `cover_image` VARCHAR(500) COMMENT '封面图片',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    INDEX `idx_city` (`city`),
    INDEX `idx_location` (`longitude`, `latitude`),
    INDEX `idx_category` (`category`),
    CONSTRAINT `chk_location_lng` CHECK (`longitude` BETWEEN -180 AND 180),
    CONSTRAINT `chk_location_lat` CHECK (`latitude` BETWEEN -90 AND 90)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点信息表';

-- ============================================================
-- 模块 3: 打卡系统
-- ============================================================
CREATE TABLE `check_in` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '打卡ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `location_id` BIGINT COMMENT '关联的地点ID(可为空)',
    `longitude` DECIMAL(10, 7) NOT NULL COMMENT '打卡经度',
    `latitude` DECIMAL(10, 7) NOT NULL COMMENT '打卡纬度',
    `location_name` VARCHAR(100) NOT NULL COMMENT '打卡地点名称',
    `content` TEXT COMMENT '打卡文字内容',
    `images` JSON COMMENT '图片URL数组',
    `check_in_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    CONSTRAINT `fk_checkin_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_checkin_location` FOREIGN KEY (`location_id`) REFERENCES `location`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_time` (`user_id`, `check_in_time`),
    INDEX `idx_check_in_time` (`check_in_time`),
    INDEX `idx_location` (`longitude`, `latitude`),
    INDEX `idx_location_id` (`location_id`),
    CONSTRAINT `chk_checkin_lng` CHECK (`longitude` BETWEEN -180 AND 180),
    CONSTRAINT `chk_checkin_lat` CHECK (`latitude` BETWEEN -90 AND 90)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户打卡记录表';

CREATE TABLE `check_in_like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    `check_in_id` BIGINT NOT NULL COMMENT '打卡记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE KEY `uk_checkin_user` (`check_in_id`, `user_id`),
    CONSTRAINT `fk_checkinlike_checkin` FOREIGN KEY (`check_in_id`) REFERENCES `check_in`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_checkinlike_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_check_in_id` (`check_in_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡点赞表';

-- ============================================================
-- 模块 4: 评论系统
-- ============================================================
CREATE TABLE `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    `check_in_id` BIGINT NOT NULL COMMENT '打卡ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（NULL=顶级评论）',
    `reply_to_id` BIGINT DEFAULT NULL COMMENT '被回复的评论ID',
    `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '被回复的用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '1=正常 0=已删除 -1=审核中',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_comment_checkin` FOREIGN KEY (`check_in_id`) REFERENCES `check_in`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_comment_reply_to` FOREIGN KEY (`reply_to_id`) REFERENCES `comment`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_comment_reply_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_check_in` (`check_in_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_parent` (`parent_id`),
    INDEX `idx_created` (`created_at`),
    INDEX `idx_reply_to_id` (`reply_to_id`),
    INDEX `idx_reply_to_user_id` (`reply_to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE `comment_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    CONSTRAINT `fk_commentlike_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_commentlike_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_comment` (`comment_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- ============================================================
-- 模块 5: 关注系统
-- ============================================================
CREATE TABLE `follow` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关注ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
    `followee_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `status` TINYINT DEFAULT 1 COMMENT '1=正常 0=取消',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_follow` (`follower_id`, `followee_id`),
    CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_follower` (`follower_id`),
    INDEX `idx_followee` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系表';

-- ============================================================
-- 模块 6: 通知系统
-- ============================================================
CREATE TABLE `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收者ID',
    `from_user_id` BIGINT DEFAULT NULL COMMENT '触发者ID',
    `type` VARCHAR(32) NOT NULL COMMENT '通知类型: LIKE/COMMENT/REPLY/FOLLOW/SYSTEM/ACHIEVEMENT',
    `target_type` VARCHAR(32) DEFAULT NULL COMMENT '目标类型: CHECK_IN/COMMENT/TRAVEL_NOTE',
    `target_id` BIGINT DEFAULT NULL COMMENT '目标ID',
    `content` TEXT DEFAULT NULL COMMENT '通知摘要',
    `is_read` TINYINT DEFAULT 0 COMMENT '0=未读 1=已读',
    `status` TINYINT DEFAULT 1 COMMENT '1=正常 0=已删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
    CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_notification_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_user_read` (`user_id`, `is_read`, `created_at`),
    INDEX `idx_created` (`created_at`),
    INDEX `idx_from_user_id` (`from_user_id`),
    INDEX `idx_target` (`target_type`, `target_id`),
    CONSTRAINT `chk_notification_read` CHECK (`is_read` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================================
-- 模块 7: 话题/标签系统
-- ============================================================
CREATE TABLE `topic` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '话题ID',
    `name` VARCHAR(64) NOT NULL COMMENT '话题名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '话题描述',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
    `check_in_count` INT DEFAULT 0 COMMENT '关联打卡数',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_name` (`name`),
    INDEX `idx_hot` (`is_hot`, `check_in_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题/标签表';

CREATE TABLE `check_in_topic` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `check_in_id` BIGINT NOT NULL COMMENT '打卡ID',
    `topic_id` BIGINT NOT NULL COMMENT '话题ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关联时间',
    UNIQUE KEY `uk_checkin_topic` (`check_in_id`, `topic_id`),
    CONSTRAINT `fk_checkintopic_checkin` FOREIGN KEY (`check_in_id`) REFERENCES `check_in`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_checkintopic_topic` FOREIGN KEY (`topic_id`) REFERENCES `topic`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_topic` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡-话题关联表';

-- ============================================================
-- 模块 8: 成就系统
-- ============================================================
CREATE TABLE `achievement` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成就ID',
    `code` VARCHAR(64) NOT NULL COMMENT '成就编码',
    `name` VARCHAR(128) NOT NULL COMMENT '成就名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '成就描述',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '徽章图标URL',
    `category` VARCHAR(32) NOT NULL COMMENT '分类: STREAK/CITY/LIKE/CHECK_IN/SOCIAL/SPECIAL',
    `level` TINYINT DEFAULT 1 COMMENT '等级',
    `condition_type` VARCHAR(64) NOT NULL COMMENT '触发条件类型',
    `condition_value` INT NOT NULL COMMENT '触发条件阈值',
    `points_reward` INT DEFAULT 0 COMMENT '奖励积分',
    `is_hidden` TINYINT DEFAULT 0 COMMENT '是否隐藏',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_code` (`code`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就定义表';

CREATE TABLE `user_achievement` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户成就ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `achievement_id` BIGINT NOT NULL COMMENT '成就ID',
    `progress` INT DEFAULT 0 COMMENT '当前进度',
    `is_unlocked` TINYINT DEFAULT 0 COMMENT '是否已解锁',
    `unlocked_at` DATETIME DEFAULT NULL COMMENT '解锁时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
    CONSTRAINT `fk_userachievement_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_userachievement_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `achievement`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_achievement_id` (`achievement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- ============================================================
-- 模块 9: 游记系统
-- ============================================================
CREATE TABLE `travel_note` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '游记ID',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(255) NOT NULL COMMENT '游记标题',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `content` TEXT NOT NULL COMMENT '正文(富文本)',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    `tags` JSON DEFAULT NULL COMMENT '标签JSON数组',
    `check_in_point_ids` JSON DEFAULT NULL COMMENT '关联打卡点ID数组',
    `status` TINYINT DEFAULT 1 COMMENT '1=已发布 0=草稿 -1=审核中',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `is_pinned` TINYINT DEFAULT 0 COMMENT '是否置顶',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_travelnote_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_status_time` (`status`, `created_at`),
    INDEX `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记表';

CREATE TABLE `travel_note_image` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    `note_id` BIGINT NOT NULL COMMENT '游记ID',
    `url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    CONSTRAINT `fk_travelnoteimage_note` FOREIGN KEY (`note_id`) REFERENCES `travel_note`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_note` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记图片表';

CREATE TABLE `travel_note_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    `note_id` BIGINT NOT NULL COMMENT '游记ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE KEY `uk_note_user` (`note_id`, `user_id`),
    CONSTRAINT `fk_travelnotelike_note` FOREIGN KEY (`note_id`) REFERENCES `travel_note`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_travelnotelike_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_note` (`note_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记点赞表';

CREATE TABLE `travel_note_collect` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    `note_id` BIGINT NOT NULL COMMENT '游记ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    UNIQUE KEY `uk_note_user` (`note_id`, `user_id`),
    CONSTRAINT `fk_travelnotecollect_note` FOREIGN KEY (`note_id`) REFERENCES `travel_note`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_travelnotecollect_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_note_id` (`note_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记收藏表';

-- ============================================================
-- 模块 10: 积分与会员体系
-- ============================================================
CREATE TABLE `user_point` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '积分ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_points` INT DEFAULT 0 COMMENT '累计积分',
    `current_points` INT DEFAULT 0 COMMENT '当前可用积分',
    `level` TINYINT DEFAULT 0 COMMENT '0=普通 1=青铜 2=白银 3=黄金 4=铂金',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user` (`user_id`),
    CONSTRAINT `fk_userpoint_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

CREATE TABLE `point_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(32) NOT NULL COMMENT '类型: CHECK_IN/LIKE_RECEIVED/COMMENT/FOLLOW/ACHIEVEMENT/DAILY/SYSTEM',
    `points` INT NOT NULL COMMENT '正数=获得 负数=消耗',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `ref_type` VARCHAR(32) DEFAULT NULL COMMENT '关联类型',
    `ref_id` BIGINT DEFAULT NULL COMMENT '关联ID',
    `balance_after` INT DEFAULT NULL COMMENT '操作后余额',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT `fk_pointrecord_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_user_time` (`user_id`, `created_at`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- ============================================================
-- 模块 11: 商家推荐
-- ============================================================
CREATE TABLE `merchant_position` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商家ID',
    `name` VARCHAR(255) NOT NULL COMMENT '商家名称',
    `category` VARCHAR(64) NOT NULL COMMENT '分类: 餐饮/住宿/购物/娱乐/交通',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '详细地址',
    `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
    `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    `rating` DECIMAL(2,1) DEFAULT 0 COMMENT '评分 0-5',
    `price_level` TINYINT DEFAULT 1 COMMENT '1=经济 2=中等 3=高端',
    `tags` JSON DEFAULT NULL COMMENT '标签 ["wifi","停车"]',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '电话',
    `business_hours` VARCHAR(255) DEFAULT NULL COMMENT '营业时间',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '1=正常 0=下线',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_location` (`longitude`, `latitude`),
    INDEX `idx_city_category` (`city`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐商家位置表';

-- ============================================================
-- 模块 12: 行程规划
-- ============================================================
CREATE TABLE `trip_plan` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '行程ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '行程标题',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '行程描述',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '目的地城市',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `start_date` DATE DEFAULT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '结束日期',
    `total_days` INT DEFAULT 0 COMMENT '总天数',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开',
    `status` TINYINT DEFAULT 1 COMMENT '1=规划中 2=进行中 3=已完成 0=已删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_tripplan_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_public` (`is_public`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程计划表';

CREATE TABLE `trip_day` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日程ID',
    `plan_id` BIGINT NOT NULL COMMENT '行程ID',
    `day_number` INT NOT NULL COMMENT '第几天',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '当天主题',
    `date` DATE DEFAULT NULL COMMENT '日期',
    `budget` DECIMAL(10,2) DEFAULT NULL COMMENT '预算',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT `fk_tripday_plan` FOREIGN KEY (`plan_id`) REFERENCES `trip_plan`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_plan` (`plan_id`, `day_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程日表';

CREATE TABLE `trip_poi` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'POI ID',
    `day_id` BIGINT NOT NULL COMMENT '日程ID',
    `plan_id` BIGINT NOT NULL COMMENT '行程ID',
    `name` VARCHAR(255) NOT NULL COMMENT '地点名称',
    `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
    `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '地址',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '分类: 景点/餐饮/住宿',
    `notes` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `duration_minutes` INT DEFAULT 60 COMMENT '预计停留时间',
    `sort_order` INT DEFAULT 0 COMMENT '当天顺序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT `fk_trippoi_day` FOREIGN KEY (`day_id`) REFERENCES `trip_day`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_trippoi_plan` FOREIGN KEY (`plan_id`) REFERENCES `trip_plan`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_day` (`day_id`, `sort_order`),
    INDEX `idx_plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程POI点表';

-- ============================================================
-- 模块 13: 内容审核
-- ============================================================
CREATE TABLE `sensitive_word` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '词ID',
    `word` VARCHAR(128) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(32) DEFAULT 'GENERAL' COMMENT '分类: GENERAL/POLITICAL/PORN/VIOLENCE/SPAM',
    `level` TINYINT DEFAULT 1 COMMENT '处理级别: 1=审核 2=替换 3=拒绝',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

CREATE TABLE `report` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '举报ID',
    `reporter_id` BIGINT NOT NULL COMMENT '举报者ID',
    `reported_user_id` BIGINT NOT NULL COMMENT '被举报者ID',
    `target_type` VARCHAR(32) NOT NULL COMMENT '目标类型: CHECK_IN/COMMENT/TRAVEL_NOTE/USER',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `reason` VARCHAR(255) NOT NULL COMMENT '举报原因',
    `detail` TEXT DEFAULT NULL COMMENT '详细描述',
    `status` TINYINT DEFAULT 0 COMMENT '0=待处理 1=已处理 -1=驳回',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_report_reported` FOREIGN KEY (`reported_user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_status` (`status`, `created_at`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_reporter` (`reporter_id`),
    INDEX `idx_reported_user_id` (`reported_user_id`),
    INDEX `idx_handler_id` (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

CREATE TABLE `image_audit` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审核ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `uploader_id` BIGINT NOT NULL COMMENT '上传者ID',
    `source_type` VARCHAR(32) NOT NULL COMMENT '来源: CHECK_IN/COMMENT/TRAVEL_NOTE/AVATAR',
    `source_id` BIGINT NOT NULL COMMENT '来源ID',
    `audit_status` TINYINT DEFAULT 0 COMMENT '0=待审核 1=通过 -1=违规',
    `audit_result` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT `fk_imageaudit_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_imageaudit_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_status` (`audit_status`, `created_at`),
    INDEX `idx_uploader` (`uploader_id`),
    INDEX `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片审核表';

-- ============================================================
-- 种子数据: 示例地点
-- ============================================================
INSERT INTO `location` (`name`, `address`, `longitude`, `latitude`, `category`, `city`, `description`) VALUES
('天安门广场', '北京市东城区东长安街', 116.397477, 39.903738, '景点', '北京', '世界上最大的城市广场，中国的象征'),
('故宫博物院', '北京市东城区景山前街4号', 116.397026, 39.916345, '景点', '北京', '中国明清两代的皇家宫殿，世界文化遗产'),
('长城(八达岭)', '北京市延庆区G6京藏高速58号出口', 116.016937, 40.353568, '景点', '北京', '世界文化遗产，中国古代伟大的防御工程'),
('外滩', '上海市黄浦区中山东一路', 121.490917, 31.237476, '景点', '上海', '上海的标志性景点，万国建筑博览群'),
('东方明珠', '上海市浦东新区世纪大道1号', 121.495619, 31.239720, '景点', '上海', '上海标志性建筑，高468米的广播电视塔'),
('西湖', '浙江省杭州市西湖区龙井路1号', 120.146631, 30.244275, '景点', '杭州', '中国著名的风景名胜区，世界文化遗产'),
('兵马俑', '陕西省西安市临潼区秦陵北路', 109.278469, 34.384122, '景点', '西安', '世界第八大奇迹，秦始皇陵陪葬坑'),
('九寨沟', '四川省阿坝藏族羌族自治州九寨沟县', 103.918429, 33.260028, '景点', '阿坝', '世界自然遗产，以翠海、叠瀑、彩林、雪峰、藏情、蓝冰六绝闻名');

-- ============================================================
-- 种子数据: 测试用户 (密码: 123456)
-- BCrypt哈希由 BCryptPasswordEncoder.encode("123456") 生成
-- ============================================================
INSERT INTO `user` (`account`, `username`, `password`, `email`, `role`) VALUES
('test', 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'test@example.com', 'USER'),
('admin', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'admin@example.com', 'ADMIN');

-- ============================================================
-- 种子数据: 成就定义
-- ============================================================
INSERT INTO `achievement` (`code`, `name`, `description`, `category`, `level`, `condition_type`, `condition_value`, `points_reward`) VALUES
('FIRST_CHECK_IN', '初次打卡', '完成第一次打卡', 'CHECK_IN', 1, 'CHECK_IN_COUNT', 1, 10),
('CHECK_IN_10', '打卡达人', '累计打卡10次', 'CHECK_IN', 1, 'CHECK_IN_COUNT', 10, 50),
('CHECK_IN_50', '打卡高手', '累计打卡50次', 'CHECK_IN', 2, 'CHECK_IN_COUNT', 50, 100),
('CHECK_IN_100', '打卡大师', '累计打卡100次', 'CHECK_IN', 3, 'CHECK_IN_COUNT', 100, 200),
('STREAK_3', '连续打卡3天', '连续3天打卡', 'STREAK', 1, 'STREAK_DAYS', 3, 30),
('STREAK_7', '周打卡王', '连续7天打卡', 'STREAK', 2, 'STREAK_DAYS', 7, 80),
('STREAK_30', '月度打卡王', '连续30天打卡', 'STREAK', 3, 'STREAK_DAYS', 30, 300),
('CITY_3', '三城记', '在3个不同城市打卡', 'CITY', 1, 'CITY_COUNT', 3, 50),
('CITY_10', '十城记', '在10个不同城市打卡', 'CITY', 2, 'CITY_COUNT', 10, 150),
('LIKE_10', '初获认可', '累计获得10个点赞', 'LIKE', 1, 'LIKE_RECEIVED', 10, 20),
('LIKE_100', '人气之星', '累计获得100个点赞', 'LIKE', 2, 'LIKE_RECEIVED', 100, 100),
('FOLLOW_10', '社交达人', '关注10个用户', 'SOCIAL', 1, 'FOLLOW_COUNT', 10, 20),
('COMMENT_10', '评论家', '发表10条评论', 'SOCIAL', 1, 'COMMENT_COUNT', 10, 30),
('TRAVEL_NOTE_1', '游记作家', '发表第一篇游记', 'SPECIAL', 1, 'TRAVEL_NOTE_COUNT', 1, 50);

-- ============================================================
-- 种子数据: 敏感词
-- ============================================================
INSERT INTO `sensitive_word` (`word`, `category`, `level`) VALUES
('赌博', 'GENERAL', 3),
('色情', 'PORN', 3),
('暴力', 'VIOLENCE', 3),
('诈骗', 'SPAM', 3);

-- ============================================================
-- 种子数据: 示例商家
-- ============================================================
INSERT INTO `merchant_position` (`name`, `category`, `address`, `longitude`, `latitude`, `city`, `rating`, `price_level`) VALUES
('全聚德烤鸭店(前门店)', '餐饮', '北京市东城区前门大街32号', 116.397128, 39.899635, '北京', 4.5, 3),
('海底捞火锅(王府井店)', '餐饮', '北京市东城区王府井大街218号', 116.412345, 39.915678, '北京', 4.3, 2),
('外婆家(西湖银泰店)', '餐饮', '杭州市上城区延安路98号', 120.164567, 30.243210, '杭州', 4.2, 2),
('上海外滩悦榕庄', '住宿', '上海市黄浦区中山东一路1号', 121.491234, 31.238901, '上海', 4.8, 3),
('北京王府井希尔顿酒店', '住宿', '北京市东城区王府井大街99号', 116.410123, 39.913456, '北京', 4.6, 3);

-- ============================================================
-- 视图: 用户统计摘要
-- ============================================================
CREATE OR REPLACE VIEW `v_user_stats` AS
SELECT
    u.id AS user_id,
    u.account,
    u.username,
    COUNT(DISTINCT ci.id) AS check_in_count,
    COUNT(DISTINCT cil.id) AS like_received,
    COUNT(DISTINCT f1.follower_id) AS follower_count,
    COUNT(DISTINCT f2.followee_id) AS following_count,
    COALESCE(up.total_points, 0) AS total_points,
    COALESCE(up.level, 0) AS member_level
FROM user u
LEFT JOIN check_in ci ON ci.user_id = u.id AND ci.deleted = 0
LEFT JOIN check_in_like cil ON cil.check_in_id = ci.id
LEFT JOIN follow f1 ON f1.followee_id = u.id AND f1.status = 1
LEFT JOIN follow f2 ON f2.follower_id = u.id AND f2.status = 1
LEFT JOIN user_point up ON up.user_id = u.id
WHERE u.deleted = 0
GROUP BY u.id, u.account, u.username, up.total_points, up.level;

-- ============================================================
-- 完成
-- ============================================================
SHOW TABLES;
SELECT '数据库初始化完成！' AS message;
