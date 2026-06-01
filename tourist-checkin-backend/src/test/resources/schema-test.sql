-- H2 测试库初始化（兼容 MODE=MySQL）
-- 仅包含并发测试所需的核心表

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `avatar` VARCHAR(500),
    `email` VARCHAR(100),
    `role` VARCHAR(20) DEFAULT 'USER',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    UNIQUE KEY `uk_username` (`username`)
);

CREATE TABLE IF NOT EXISTS `check_in` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `location_id` BIGINT,
    `longitude` DECIMAL(10, 7) NOT NULL,
    `latitude` DECIMAL(10, 7) NOT NULL,
    `location_name` VARCHAR(100) NOT NULL,
    `content` TEXT,
    `images` TEXT,
    `check_in_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `check_in_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `check_in_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_checkin_user` (`check_in_id`, `user_id`)
);

CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `check_in_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `reply_to_id` BIGINT DEFAULT NULL,
    `reply_to_user_id` BIGINT DEFAULT NULL,
    `content` TEXT NOT NULL,
    `like_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comment_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
);

CREATE TABLE IF NOT EXISTS `user_point` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `total_points` INT DEFAULT 0,
    `current_points` INT DEFAULT 0,
    `level` TINYINT DEFAULT 0,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user` (`user_id`)
);

CREATE TABLE IF NOT EXISTS `point_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `type` VARCHAR(32) NOT NULL,
    `points` INT NOT NULL,
    `description` VARCHAR(255),
    `ref_type` VARCHAR(32),
    `ref_id` BIGINT,
    `balance_after` INT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `word` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50),
    `level` INT DEFAULT 1,
    `is_enabled` TINYINT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `from_user_id` BIGINT,
    `type` VARCHAR(32) NOT NULL,
    `target_type` VARCHAR(32),
    `target_id` BIGINT,
    `content` VARCHAR(500),
    `is_read` TINYINT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试用户
INSERT INTO `user` (`username`, `password`, `email`, `role`) VALUES
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'test@example.com', 'USER'),
('other', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'other@example.com', 'USER');

-- 插入测试打卡
INSERT INTO `check_in` (`user_id`, `location_id`, `longitude`, `latitude`, `location_name`, `content`, `check_in_time`)
VALUES (2, NULL, 116.397428, 39.90923, '测试地点', '测试打卡内容', CURRENT_TIMESTAMP);

-- 插入测试评论
INSERT INTO `comment` (`check_in_id`, `user_id`, `content`, `like_count`, `status`)
VALUES (1, 2, '测试评论', 0, 1);
