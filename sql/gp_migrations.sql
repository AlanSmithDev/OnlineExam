-- gp_migrations.sql
-- 幂等迁移脚本：确保表/列存在，避免重复执行报错
-- 运行方式：在 MySQL 中 USE gp; SOURCE gp_migrations.sql;

SET @schema := DATABASE();

-- Ensure auth.id is BIGINT
-- If auth table exists and id is not BIGINT, attempt to alter it. This statement checks INFORMATION_SCHEMA.
-- Note: Some MySQL versions do not allow IF in ALTER; we use conditional SELECT to build statement if needed.

-- Add exam_intro, exam_notice, camera_required, class_scope to test if missing
-- Add avatar columns to student/teacher if missing

-- Add exam_intro
SELECT IF(COUNT(*)=0, 'ALTER TABLE `test` ADD COLUMN `exam_intro` TEXT NULL COMMENT "开头信息/考试说明";', 'SELECT "exam_intro exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='test' AND COLUMN_NAME='exam_intro';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add exam_notice
SELECT IF(COUNT(*)=0, 'ALTER TABLE `test` ADD COLUMN `exam_notice` TEXT NULL COMMENT "考试注意事项";', 'SELECT "exam_notice exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='test' AND COLUMN_NAME='exam_notice';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add camera_required
SELECT IF(COUNT(*)=0, 'ALTER TABLE `test` ADD COLUMN `camera_required` TINYINT(1) NOT NULL DEFAULT 1 COMMENT "是否要求摄像头/AI监控 1是 0否";', 'SELECT "camera_required exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='test' AND COLUMN_NAME='camera_required';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add class_scope
SELECT IF(COUNT(*)=0, 'ALTER TABLE `test` ADD COLUMN `class_scope` VARCHAR(255) NULL DEFAULT NULL COMMENT "可见班级（中文显示）";', 'SELECT "class_scope exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='test' AND COLUMN_NAME='class_scope';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure student.avatar_blob
SELECT IF(COUNT(*)=0, 'ALTER TABLE `student` ADD COLUMN `avatar_blob` MEDIUMBLOB NULL COMMENT "头像二进制数据";', 'SELECT "student.avatar_blob exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='student' AND COLUMN_NAME='avatar_blob';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `student` ADD COLUMN `avatar_name` VARCHAR(200) NULL;', 'SELECT "student.avatar_name exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='student' AND COLUMN_NAME='avatar_name';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `student` ADD COLUMN `avatar_mime` VARCHAR(100) NULL;', 'SELECT "student.avatar_mime exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='student' AND COLUMN_NAME='avatar_mime';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure teacher.avatar_blob and teach_class_scope
SELECT IF(COUNT(*)=0, 'ALTER TABLE `teacher` ADD COLUMN `avatar_blob` MEDIUMBLOB NULL;', 'SELECT "teacher.avatar_blob exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='teacher' AND COLUMN_NAME='avatar_blob';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `teacher` ADD COLUMN `avatar_name` VARCHAR(200) NULL;', 'SELECT "teacher.avatar_name exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='teacher' AND COLUMN_NAME='avatar_name';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `teacher` ADD COLUMN `avatar_mime` VARCHAR(100) NULL;', 'SELECT "teacher.avatar_mime exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='teacher' AND COLUMN_NAME='avatar_mime';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `teacher` ADD COLUMN `teach_class_scope` VARCHAR(500) NULL COMMENT "所教班级（中文显示，逗号分隔）";', 'SELECT "teacher.teach_class_scope exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='teacher' AND COLUMN_NAME='teach_class_scope';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure auth.id is BIGINT: if it's INT, try to alter (may fail if FK constraints); handle cautiously
SELECT DATA_TYPE INTO @id_type
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='auth' AND COLUMN_NAME='id';

SELECT IF(@id_type IS NULL, 'SELECT "auth.id missing or table not present";', IF(@id_type='bigint', 'SELECT "auth.id already BIGINT";', 'ALTER TABLE `auth` MODIFY COLUMN `id` BIGINT(20) NOT NULL;')) INTO @s;
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 通知表兜底：确保 notice 表与核心列存在（修复 notice/list 与 publish 的 500）
SELECT IF(COUNT(*)=0,
'CREATE TABLE `notice` (\n  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n  `title` varchar(200) NOT NULL,\n  `content` text NOT NULL,\n  `target_role` int(11) NOT NULL DEFAULT 0,\n  `publisher_no` varchar(50) NOT NULL,\n  `publisher_name` varchar(100) NOT NULL,\n  `publisher_role_name` varchar(50) NOT NULL DEFAULT "",\n  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,\n  PRIMARY KEY (`id`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8;',
'SELECT "notice table exists";') INTO @s
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `notice` ADD COLUMN `title` varchar(200) NOT NULL DEFAULT "";', 'SELECT "notice.title exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice' AND COLUMN_NAME='title';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `notice` ADD COLUMN `content` text NULL;', 'SELECT "notice.content exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice' AND COLUMN_NAME='content';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `notice` ADD COLUMN `target_role` int(11) NOT NULL DEFAULT 0;', 'SELECT "notice.target_role exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice' AND COLUMN_NAME='target_role';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `notice` ADD COLUMN `publisher_no` varchar(50) NOT NULL DEFAULT "";', 'SELECT "notice.publisher_no exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice' AND COLUMN_NAME='publisher_no';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `notice` ADD COLUMN `publisher_name` varchar(100) NOT NULL DEFAULT "";', 'SELECT "notice.publisher_name exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice' AND COLUMN_NAME='publisher_name';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `notice` ADD COLUMN `publisher_role_name` varchar(50) NOT NULL DEFAULT "";', 'SELECT "notice.publisher_role_name exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice' AND COLUMN_NAME='publisher_role_name';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*)=0, 'ALTER TABLE `notice` ADD COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP;', 'SELECT "notice.create_time exists";') INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notice' AND COLUMN_NAME='create_time';
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Done
SELECT 'MIGRATIONS_DONE' as status;

/* End of gp_migrations.sql */
