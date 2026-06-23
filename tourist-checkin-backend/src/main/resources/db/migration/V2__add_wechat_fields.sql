-- 微信小程序支持: 增加 openid 字段，account/password 改为可空
ALTER TABLE `user` ADD COLUMN `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信openid' AFTER `email`;
ALTER TABLE `user` ADD UNIQUE KEY `uk_openid` (`openid`);
ALTER TABLE `user` MODIFY COLUMN `account` VARCHAR(50) DEFAULT NULL COMMENT '登录账号(唯一)';
ALTER TABLE `user` MODIFY COLUMN `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(BCrypt加密)';
