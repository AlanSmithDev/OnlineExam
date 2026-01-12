/*
  Online Exam Database Init - gp

  统一编号规则（12位，清晰注释）：
  - 人员/账号/课程/考试通用：YYYY RR CC MM SS
      YYYY  : 年份（入学/入职/建课/建考）
      RR    : 角色/类型编码（10管理员，20教师，30学生，40课程，50考试）
      CC    : 学院编码（例：01 计算机学院）
      MM    : 专业/学科编码（例：01 软件工程，02 网络工程，00 表示通用/无）
      SS    : 序号/班级/流水（01起），班级/学生可继续在序号中体现班级顺序
  - 班级编号：YYYY CC MM CL SS （CL 为班级顺序，SS 为班级内流水），班级名称中文：如“2026级 软件工程1班”。
  - 学生编号示例：2026 30 01 01 01（202630010101）→ 2026级 软件工程1班 第1人。
  - 教师编号示例：2012 20 01 01 01（201220010101）。
  - 管理员编号示例：2026 10 00 00 01（202610000001）。
  - 课程编号示例：2026 40 01 01 01（course_code）。
  - 考试编号示例：2026 50 01 01 01（test_no/id）。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Create Database
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `gp` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 补充：供 SQL 检查器识别的显式建表（避免 Unable to resolve table）
DROP TABLE IF EXISTS `test_question`;
CREATE TABLE `test_question`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `test_no` bigint(20) NOT NULL COMMENT '考试编号',
  `question_no` bigint(20) NOT NULL COMMENT '题目编号(关联 choice/subjective.id)',
  `score` int(11) NOT NULL COMMENT '题目分值',
  `question_type` char(1) NOT NULL COMMENT '题型：1单选 2判断 3多选 4主观',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_test_question_test_no` (`test_no`) USING BTREE,
  KEY `idx_test_question_question_no` (`question_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sender_no` varchar(50) NOT NULL COMMENT '发送者账号',
  `sender_name` varchar(100) NOT NULL COMMENT '发送者姓名',
  `content` text NOT NULL COMMENT '消息内容',
  `target` varchar(50) DEFAULT 'all' COMMENT '消息目标 all/teacher/student',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_im_message_time` (`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
USE `gp`;

-- ----------------------------
-- Table structure for auth
-- ----------------------------
DROP TABLE IF EXISTS `auth`;
CREATE TABLE `auth`  (
                         `id` bigint(20) NOT NULL COMMENT '统一使用12位编号作为主键',
                         `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录账号，12位编号',
                         `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码（加密后）',
                         `roleId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色id，0管理员，1学生，2教师',
                         `flag` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标志，0管理员，1学生，2教师',
                         `salt` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '盐值',
                         `role_name` varchar(50) NULL DEFAULT NULL COMMENT '角色名称（管理员/教师/学生）',
                         `user_no` varchar(50) NULL COMMENT '12位用户编号（学号/教工号/管理员编号）',
                         PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- 账号示例：全部12位编号，id与username一致；password 为 Shiro MD5(123456+salt, 2轮)，salt=username


INSERT INTO `auth` (`id`, `username`, `password`, `roleId`, `flag`, `salt`, `role_name`) VALUES
    (202610000001, '202610000001', '73f8875f546d6eb3cf4376a5fba49913', '0', '0', '202610000001', '管理员'),
    (201220010101, '201220010101', 'baacb3f15ae9c5354817da6fbbe0bd43', '2', '2', '201220010101', '教师'),
    (201420010102, '201420010102', 'cde0d88a850dde36c54d196dcfe4a419', '2', '2', '201420010102', '教师'),
    (201520020101, '201520020101', '45fb684a77ffb2e220a186db70b56478', '2', '2', '201520020101', '教师'),
    (201620010103, '201620010103', 'f3422008989b72ab8c3dbfd1343b6ad4', '2', '2', '201620010103', '教师'),
    (201620020102, '201620020102', 'f386776501a8b9e31eb3bf52f843cdc9', '2', '2', '201620020102', '教师'),
    (202630010101, '202630010101', 'cc1e118a69de191b835fd230c8b01b54', '1', '1', '202630010101', '学生'),
    (202630010102, '202630010102', '134d01e3298d9a5adc0395ca7ffaf805', '1', '1', '202630010102', '学生'),
    (202630010201, '202630010201', 'd1732efabad6f833a03d6136e402541a', '1', '1', '202630010201', '学生'),
    (202630020101, '202630020101', '018e94e025d7a4aa628310c733f99204', '1', '1', '202630020101', '学生'),
    (202630010103, '202630010103', '9a1fb5c2fc612c6610e9456fa619ac89', '1', '1', '202630010103', '学生'),
    (202630020102, '202630020102', 'e8a11244e89cf683e483a8a874398ea1', '1', '1', '202630020102', '学生'),
    (202630020201, '202630020201', '3fa445f7a2aa15ca91c7dd2befe9a7b7', '1', '1', '202630020201', '学生'),
    (202630020202, '202630020202', 'e5f1b05daafe521692694a47fc343a12', '1', '1', '202630020202', '学生');

-- ----------------------------
-- Table structure for choice
-- ----------------------------


DROP TABLE IF EXISTS `choice`;
CREATE TABLE `choice`  (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `question` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '题目',
                           `choice_a` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '选项A',
                           `choice_b` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '选项B',
                           `choice_c` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '选项C',
                           `choice_d` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '选项D',
                           `answer` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '答案',
                           `course_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '课程名称',
                           `type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '题目类型：1单选，2判断，3多选',
                           `create_time` datetime(0) NULL DEFAULT NULL,
                           `update_time` datetime(0) NULL DEFAULT NULL,
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for class
-- ----------------------------
DROP TABLE IF EXISTS `class`;
CREATE TABLE `class`  (
                          `id` int(11) NOT NULL AUTO_INCREMENT,
                          `class_co` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '年级，如 2026',
                          `class_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '班级编号',
                          `class_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '班级名称，例：2026级 软件工程1班',
                          `major_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          `institute_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          `college_no` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `course_code` varchar(20) NULL DEFAULT NULL COMMENT '12位课程编号：YYYY40CCMMSS',
                           `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for institute
-- ----------------------------
DROP TABLE IF EXISTS `institute`;
CREATE TABLE `institute`  (
                              `id` int(11) NOT NULL AUTO_INCREMENT,
                              `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                              `institute_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                              `college_no` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                              PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for major
-- ----------------------------
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major`  (
                          `id` int(11) NOT NULL AUTO_INCREMENT,
                          `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          `major_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          `institute_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          `college_no` char(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for paper_status
-- ----------------------------
DROP TABLE IF EXISTS `paper_status`;
CREATE TABLE `paper_status`  (
                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                 `test_no` bigint(20) NULL DEFAULT NULL,
                                 `test_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                 `choice_status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客观题批改状态',
                                 `subjective_status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主观题批改状态',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for score
-- ----------------------------
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score`  (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `student_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                          `test_no` bigint(20) NULL DEFAULT NULL,
                          `choice` int(11) NULL DEFAULT NULL,
                          `subjective` int(11) NULL DEFAULT NULL,
                          `total` int(11) NULL DEFAULT NULL,
                          `create_time` datetime(0) NULL DEFAULT NULL,
                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for score_detail
-- ----------------------------
DROP TABLE IF EXISTS `score_detail`;
CREATE TABLE `score_detail`  (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `student_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                 `test_no` bigint(20) NULL DEFAULT NULL,
                                 `question_no` bigint(20) NULL DEFAULT NULL,
                                 `student_score` int(11) NULL DEFAULT NULL,
                                 `question_score` int(11) NULL DEFAULT NULL,
                                 `question_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                 `flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                 `create_time` datetime(0) NULL DEFAULT NULL,
                                 `student_answer` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
                                 `grader_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '认领批改教师账号',
                                 `locked_at` datetime(0) NULL DEFAULT NULL COMMENT '认领时间',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `student_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `sex` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `college_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `college_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `institute_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `institute_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `major_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `major_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `class_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `class_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `grade_year` char(4) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '入学年份/年级，如2026',
                            `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `enter_time` datetime(0) NULL DEFAULT NULL,
                            `graduate_time` datetime(0) NULL DEFAULT NULL,
                            `avatar_blob` mediumblob NULL COMMENT '头像二进制数据',
                            `avatar_name` varchar(200) NULL DEFAULT NULL COMMENT '头像文件名',
                            `avatar_mime` varchar(100) NULL DEFAULT NULL COMMENT '头像MIME类型',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for subjective
-- ----------------------------
DROP TABLE IF EXISTS `subjective`;
CREATE TABLE `subjective`  (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT,
                               `question` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                               `answer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                               `course_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                               `type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '4主观题',
                               `create_time` datetime(0) NULL DEFAULT NULL,
                               `update_time` datetime(0) NULL DEFAULT NULL,
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `teacher_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '职称/职位',
                            `sex` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `college_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `college_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `institute_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `institute_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `major_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `major_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                            `enter_time` datetime(0) NULL DEFAULT NULL,
                            `leave_time` datetime(0) NULL DEFAULT NULL,
                            `teach_class_scope` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所教班级（名称或班级编号，逗号分隔）',
                            `avatar_blob` mediumblob NULL COMMENT '头像二进制数据',
                            `avatar_name` varchar(200) NULL DEFAULT NULL COMMENT '头像文件名',
                            `avatar_mime` varchar(100) NULL DEFAULT NULL COMMENT '头像MIME类型',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `title` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知标题',
                           `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知内容',
                           `target_role` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0全部 1学生 2教师',
                           `publisher_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发布人编号',
                           `publisher_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发布人姓名',
                           `publisher_role_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发布人职称',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for teacher_course
-- ----------------------------
DROP TABLE IF EXISTS `teacher_course`;
CREATE TABLE `teacher_course`  (
                                   `id` int(11) NOT NULL AUTO_INCREMENT,
                                   `teacher_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                   `course_no` int(11) NULL DEFAULT NULL,
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test
-- ----------------------------
DROP TABLE IF EXISTS `test`;
CREATE TABLE `test`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `course_id` int(11) NULL DEFAULT NULL,
  `teacher_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `total_score` int(11) NULL DEFAULT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态：0未开始，1进行中，2已结束',
  `start_time` datetime(0) NULL DEFAULT NULL,
  `end_time` datetime(0) NULL DEFAULT NULL,
  `test_time` int(11) NULL DEFAULT NULL COMMENT '考试时长(分钟)',
  `exam_notice` text NULL COMMENT '考试注意事项',
  `exam_intro` text NULL COMMENT '开头信息/考试说明',
  `camera_required` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否要求摄像头/AI监控 1是 0否',
  `institute_no` varchar(50) NULL DEFAULT NULL,
  `institute_name` varchar(100) NULL DEFAULT NULL,
  `major_no` varchar(50) NULL DEFAULT NULL,
  `major_name` varchar(100) NULL DEFAULT NULL,
  `class_scope` varchar(255) NULL DEFAULT NULL COMMENT '可见班级（中文显示，如: 2026级 软件工程 1班; 多个用逗号分隔）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_class
-- ----------------------------
DROP TABLE IF EXISTS `test_class`;
CREATE TABLE `test_class`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `test_no` bigint(20) NULL DEFAULT NULL COMMENT '考试编号',
  `class_no` varchar(50) NULL DEFAULT NULL COMMENT '班级编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of test
-- ----------------------------
INSERT INTO `test` (`id`, `name`, `course_id`, `teacher_no`, `total_score`, `status`, `start_time`, `end_time`, `test_time`, `exam_intro`, `exam_notice`, `camera_required`, `institute_no`, `institute_name`, `major_no`, `major_name`, `class_scope`) VALUES
  (202650010101, 'Java期中考试', 1, '201220010101', 100, '2', '2026-03-10 09:00:00', '2026-03-10 11:00:00', 120, '请使用IDEA与JDK17，提交前确认代码可编译。', '开考20分钟禁止提前交卷；禁止切屏超过3次；保持摄像头开启。', 1, '0101', '计算机科学与技术学院', '010101', '软件工程', '2026级 软件工程1班,2026级 软件工程2班'),
  (202650010102, 'Java 随堂测验一', 1, '201220010101', 50, '1', '2026-03-18 09:00:00', '2026-03-18 09:30:00', 30, '答题前确认题号对应。', '关闭聊天工具；切屏累计3次将自动交卷。', 1, '0101', '计算机科学与技术学院', '010101', '软件工程', '2026级 软件工程1班'),
  (202650010103, 'Java 期末考试', 1, '201220010101', 100, '0', '2026-03-25 14:00:00', '2026-03-25 16:00:00', 120, '允许使用官方API文档。', '迟到15分钟禁止入场；保持静音；严禁代考。', 1, '0101', '计算机科学与技术学院', '010101', '软件工程', '2026级 软件工程1班,2026级 软件工程2班,2026级 软件工程3班'),
  (202650020101, '数据库系统 单元测试', 2, '201220010101', 60, '2', '2026-03-12 19:00:00', '2026-03-12 20:00:00', 60, '可使用教材随堂笔记。', '选择题仅单选；填空题注意大小写。', 0, '0101', '计算机科学与技术学院', '010102', '网络工程', '2026级 网络工程1班'),
  (202650020102, '数据库系统 期末考试', 2, '201220010101', 100, '0', '2026-03-30 09:00:00', '2026-03-30 11:00:00', 120, '请提前测试数据库连接工具。', '切屏3次自动交卷；严禁携带纸质资料。', 1, '0101', '计算机科学与技术学院', '010101', '软件工程', '2026级 软件工程1班,2026级 软件工程2班,2026级 软件工程3班'),
  (202650020201, '计算机网络 测验一', 3, '201520020101', 40, '0', '2026-03-20 10:00:00', '2026-03-20 10:40:00', 40, '仅测试前三章知识点。', '保持摄像头开启；禁止使用代理抓包工具。', 1, '0101', '计算机科学与技术学院', '010102', '网络工程', '2026级 网络工程1班,2026级 网络工程2班'),
  (202650020202, '计算机网络 期末考试', 3, '201520020101', 100, '0', '2026-04-01 09:00:00', '2026-04-01 11:00:00', 120, '请提前测试网络连接。', '切屏3次自动交卷；严禁携带电子设备。', 1, '0101', '计算机科学与技术学院', '010102', '网络工程', '2026级 网络工程1班,2026级 网络工程2班');

-- ----------------------------
-- Records of test_class（使用12位考试号+12位班级号）
-- ----------------------------
DELETE FROM `test_class`;
INSERT INTO `test_class` (`test_no`, `class_no`) VALUES
  (202650010101, '202601010101'),
  (202650010101, '202601010201'),
  (202650010102, '202601010101'),
  (202650010103, '202601010101'),
  (202650010103, '202601010201'),
  (202650010103, '202601010301'),
  (202650020101, '202601020101'),
  (202650020201, '202601020101'),
  (202650020201, '202601020201'),
  (202650020102, '202601010101'),
  (202650020102, '202601010201'),
  (202650020102, '202601010301');

-- ----------------------------
-- Table structure for question_bank (题库)
-- ----------------------------
DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '题库名称',
  `course_id` int(11) NOT NULL COMMENT '所属课程',
  `creator_no` varchar(50) DEFAULT NULL COMMENT '创建人编号',
  `strategy` varchar(50) DEFAULT 'MANUAL' COMMENT '组卷策略：MANUAL/GA等',
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='题库表';

-- ----------------------------
-- Table structure for question_bank_question (题库-题目关联，便于GA/手动组卷)
-- ----------------------------
DROP TABLE IF EXISTS `question_bank_question`;
CREATE TABLE `question_bank_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bank_id` bigint(20) NOT NULL,
  `question_type` char(1) NOT NULL COMMENT '1选择，2判断，3多选，4主观',
  `question_no` bigint(20) NOT NULL COMMENT '题目编号，对应 choice/subjective',
  `difficulty` decimal(3,2) DEFAULT NULL COMMENT '难度(0-1)',
  `knowledge_tag` varchar(200) DEFAULT NULL COMMENT '知识点标签',
  `score` int(11) DEFAULT NULL COMMENT '默认分值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='题库题目关联';

-- ----------------------------
-- Table structure for test_bank (考试与题库绑定，支撑手动/GA组卷)
-- ----------------------------
DROP TABLE IF EXISTS `test_bank`;
CREATE TABLE `test_bank` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `test_no` bigint(20) NOT NULL COMMENT '考试编号',
  `bank_id` bigint(20) NOT NULL COMMENT '题库编号',
  `use_strategy` varchar(50) DEFAULT 'MANUAL' COMMENT 'MANUAL/GA',
  `ga_config` text DEFAULT NULL COMMENT '遗传算法参数(JSON)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='考试-题库绑定';

-- ----------------------------
-- 基础字典数据
-- ----------------------------

-- 院系 / 专业 / 班级
INSERT INTO `institute` (`id`, `name`, `institute_no`, `college_no`) VALUES
    (1, '计算机科学与技术学院', '0101', '2026');

INSERT INTO `major` (`id`, `name`, `major_no`, `institute_no`, `college_no`) VALUES
                                                                                 (1, '软件工程',    '010101', '0101', '2026'),
                                                                                 (2, '网络工程',    '010102', '0101', '2026');

INSERT INTO `class` (`id`, `class_co`, `class_no`, `class_name`, `major_no`, `institute_no`, `college_no`) VALUES
    (1, '2026', '202601010101', '2026级 软件工程1班', '010101', '0101', '2026'),
    (2, '2026', '202601010201', '2026级 软件工程2班', '010101', '0101', '2026'),
    (3, '2026', '202601020101', '2026级 网络工程1班', '010102', '0101', '2026');

-- ----------------------------
-- 教师数据（teacher_no 按 4位年 + 2 + 4位序号）
-- ----------------------------
INSERT INTO `teacher` (`id`, `teacher_no`, `name`, `title`, `sex`, `college_no`, `college_name`,
                       `institute_no`, `institute_name`, `major_no`, `major_name`, `phone`, `email`, `enter_time`, `leave_time`, `teach_class_scope`) VALUES
                                                                                                                                     (1, '202620010101', '教师A', '副教授', '女', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '13900139000', 'teacherA@test.com', '2012-09-01 00:00:00', NULL, '2026级 软件工程1班,2026级 软件工程2班'),
                                                                                                                                     (2, '202620010102', '教师B', '讲师', '男', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '13900139001', 'teacherB@test.com', '2014-09-01 00:00:00', NULL, '2026级 软件工程1班'),
                                                                                                                                     (3, '202620020101', '教师C', '教授', '男', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010102', '网络工程', '13900139002', 'teacherC@test.com', '2015-09-01 00:00:00', NULL, '2026级 网络工程1班,2026级 网络工程2班');

-- ----------------------------
-- 通知中心示例数据（管理员与教师）
-- ----------------------------
INSERT INTO `notice` (`title`, `content`, `target_role`, `publisher_no`, `publisher_name`, `publisher_role_name`) VALUES
('期中考试通知', '软件工程专业本周开始期中考试，请学生务必提前 30 分钟签到，迟到 15 分钟不得入场。', 1, '202610001', '管理员', '管理员'),
('阅卷进度提醒', '各位教师请于 3 月 22 日前完成 Java 期中试卷批改，并在系统中提交成绩单。', 2, '201220001', '教师A', '教师');

-- ----------------------------
-- 课程数据
-- ----------------------------
INSERT INTO `course` (`id`, `course_code`, `name`) VALUES
  (1, '202640010101', 'Java语言程序设计'),
  (2, '202640010201', '数据库系统原理'),
  (3, '202640010202', '计算机网络基础');

-- ----------------------------
-- 教师授课关系
-- ----------------------------
INSERT INTO `teacher_course` (`id`, `teacher_no`, `course_no`) VALUES
  (1, '201220010101', 1),
  (2, '201220010101', 2),
  (3, '201520020101', 1),
  (4, '201520020101', 3),
  (5, '201620010103', 1),
  (6, '201620020102', 3);

-- ----------------------------
-- 学生数据（student_no 按 12位：年份+角色30+学院+专业+序号）
-- ----------------------------
INSERT INTO `student` (`id`, `student_no`, `name`, `sex`, `college_no`, `college_name`,
                       `institute_no`, `institute_name`, `major_no`, `major_name`, `class_no`, `class_name`, `grade_year`,
                       `phone`, `email`, `enter_time`, `graduate_time`) VALUES
    (1, '202630010101', '学生A', '男', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '202601010101', '2026级 软件工程1班', '2026', '13800138001', 'stuA@test.com', '2026-09-01 00:00:00', NULL),
    (2, '202630010102', '学生B', '女', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '202601010101', '2026级 软件工程1班', '2026', '13800138002', 'stuB@test.com', '2026-09-01 00:00:00', NULL),
    (3, '202630010201', '学生C', '男', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '202601010201', '2026级 软件工程2班', '2026', '13800138003', 'stuC@test.com', '2026-09-01 00:00:00', NULL),
    (4, '202630020101', '学生D', '女', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010102', '网络工程', '202601020101', '2026级 网络工程1班', '2026', '13800138004', 'stuD@test.com', '2026-09-01 00:00:00', NULL);

-- ----------------------------
-- 额外示例数据：丰富班级 / 学生 / 教师信息，便于统计
-- ----------------------------
-- 增加班级
INSERT INTO `class` (`class_co`, `class_no`, `class_name`, `major_no`, `institute_no`, `college_no`) VALUES
  ('2026', '202601010301', '2026级 软件工程3班', '010101', '0101', '2026'),
  ('2026', '202601020201', '2026级 网络工程2班', '010102', '0101', '2026');

-- 增加教师
INSERT INTO `teacher` (`teacher_no`, `name`, `sex`, `college_no`, `college_name`, `institute_no`, `institute_name`, `major_no`, `major_name`, `phone`, `email`, `enter_time`, `leave_time`, `teach_class_scope`) VALUES
  ('201620010103', '教师D', '女', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '13900139003', 'teacherD@test.com', '2016-09-01 00:00:00', NULL, '2026级 软件工程3班'),
  ('201620020102', '教师E', '男', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010102', '网络工程', '13900139004', 'teacherE@test.com', '2016-09-01 00:00:00', NULL, '2026级 网络工程2班');

-- 对应 auth 表增加登录账号（id 与 username 同为 12 位编号）
INSERT INTO `auth` (`id`, `username`, `password`, `roleId`, `flag`, `salt`, `role_name`) VALUES
  (202620010103, '202620010103', '66996655deaace391e629f864a1f8880', '2', '2', '202620010103', '教师'),
  (202620020102, '202620020102', 'cb377f179d49c9ce88cf3b8098bf59c0', '2', '2', '202620020102', '教师');

-- 增加若干学生，覆盖不同班级
INSERT INTO `student` (`student_no`, `name`, `sex`, `college_no`, `college_name`, `institute_no`, `institute_name`, `major_no`, `major_name`, `class_no`, `class_name`, `grade_year`, `phone`, `email`, `enter_time`, `graduate_time`) VALUES
  ('202630010103', '学生E', '男', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '202601010201', '2026级 软件工程2班', '2026', '13800138005', 'stuE@test.com', '2026-09-01 00:00:00', NULL),
  ('202630020102', '学生F', '女', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010101', '软件工程', '202601010301', '2026级 软件工程3班', '2026', '13800138006', 'stuF@test.com', '2026-09-01 00:00:00', NULL),
  ('202630020201', '学生G', '男', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010102', '网络工程', '202601020201', '2026级 网络工程2班', '2026', '13800138007', 'stuG@test.com', '2026-09-01 00:00:00', NULL),
  ('202630020202', '学生H', '女', '2026', 'XX大学', '0101', '计算机科学与技术学院', '010102', '网络工程', '202601020201', '2026级 网络工程2班', '2026', '13800138008', 'stuH@test.com', '2026-09-01 00:00:00', NULL);

-- 说明：上述学生 auth 账号已包含在主 auth INSERT 中，勿重复插入。

-- 迁移：老库补充新增字段，避免 Unknown column 报错（兼容 MySQL 5.7，重复执行安全）
DROP PROCEDURE IF EXISTS add_exam_fields;
DELIMITER $$
CREATE PROCEDURE add_exam_fields()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'test'
       AND COLUMN_NAME = 'exam_notice') THEN
    ALTER TABLE `test` ADD COLUMN `exam_notice` TEXT NULL COMMENT '考试注意事项' AFTER `test_time`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'test'
       AND COLUMN_NAME = 'exam_intro') THEN
    ALTER TABLE `test` ADD COLUMN `exam_intro` TEXT NULL COMMENT '试卷开头信息/考前说明' AFTER `exam_notice`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'test'
       AND COLUMN_NAME = 'camera_required') THEN
    ALTER TABLE `test` ADD COLUMN `camera_required` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否要求摄像头/AI监控 1是 0否' AFTER `exam_intro`;
  END IF;
END$$
DELIMITER ;
CALL add_exam_fields();
DROP PROCEDURE IF EXISTS add_exam_fields;

DROP PROCEDURE IF EXISTS add_teacher_teach_class;
DELIMITER $$
CREATE PROCEDURE add_teacher_teach_class()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'teacher'
       AND COLUMN_NAME = 'teach_class_scope') THEN
    ALTER TABLE `teacher` ADD COLUMN `teach_class_scope` varchar(500) NULL COMMENT '所教班级（名称或班级编号，逗号分隔）' AFTER `leave_time`;
  END IF;
END$$
DELIMITER ;
CALL add_teacher_teach_class();
DROP PROCEDURE IF EXISTS add_teacher_teach_class;
-- 迁移结束

-- ==========================
-- 以下为合并自：alter_add_test_columns.sql
-- 已用存储过程实现条件列检查，避免 ADD COLUMN IF NOT EXISTS 可能在某些 MySQL 版本中不被支持。
-- ==========================
USE `gp`;

-- 上面已有存储过程 add_exam_fields，它负责为 test 表添加缺失列；这里不再使用不兼容的 ALTER ... ADD COLUMN IF NOT EXISTS。
-- 为保证 notice 表字段也可安全添加，创建并调用一个存储过程 add_notice_fields：

DROP PROCEDURE IF EXISTS add_notice_fields;
DELIMITER $$
CREATE PROCEDURE add_notice_fields()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'notice'
       AND COLUMN_NAME = 'publisher_no') THEN
    ALTER TABLE `notice` ADD COLUMN `publisher_no` VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '发布人编号' AFTER `target_role`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'notice'
       AND COLUMN_NAME = 'publisher_name') THEN
    ALTER TABLE `notice` ADD COLUMN `publisher_name` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '发布人姓名' AFTER `publisher_no`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'notice'
       AND COLUMN_NAME = 'publisher_role_name') THEN
    ALTER TABLE `notice` ADD COLUMN `publisher_role_name` VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '发布人职称' AFTER `publisher_name`;
  END IF;
END$$
DELIMITER ;
CALL add_notice_fields();
DROP PROCEDURE IF EXISTS add_notice_fields;

-- 注：gp.sql 文件中现已包含了 add_exam_fields() 与 add_teacher_teach_class() 存储过程及其调用，
-- 因此无需重复向 test/teacher 添加列。保留这些存储过程的存在确保在旧库上幂等迁移。

-- ==========================
-- fix_auth_id_bigint 部分已合并（在文件上方）。不再重复 ALTER。
-- End of merged migration

-- ==============================
-- Idempotent migrations to fix schema mismatches found at runtime
-- - Ensure auth.id is BIGINT to support 12-digit unified IDs
-- - Ensure `test` table has required fields used by mappers: exam_intro, exam_notice, camera_required, institute_no, institute_name, major_no, major_name, class_scope
-- The following block will add columns only if they do not already exist. It uses INFORMATION_SCHEMA and dynamic SQL so running this script multiple times is safe.

DELIMITER $$
DROP PROCEDURE IF EXISTS add_missing_test_columns$$
CREATE PROCEDURE add_missing_test_columns()
BEGIN
  -- exam_intro (LONGTEXT)
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'exam_intro') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `exam_intro` LONGTEXT NULL DEFAULT NULL COMMENT '试卷开头信息/考前说明' AFTER `exam_notice`;
  END IF;

  -- camera_required (tinyint)
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'camera_required') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `camera_required` TINYINT(1) NULL DEFAULT 0 COMMENT '是否要求摄像头/AI监控 1是 0否' AFTER `exam_intro`;
  END IF;

  -- institute_no / institute_name / major_no / major_name / class_scope: add if missing (string fields)
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'institute_no') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `institute_no` VARCHAR(50) NULL DEFAULT NULL AFTER `camera_required`;
  END IF;
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'institute_name') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `institute_name` VARCHAR(100) NULL DEFAULT NULL AFTER `institute_no`;
  END IF;
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'major_no') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `major_no` VARCHAR(50) NULL DEFAULT NULL AFTER `institute_name`;
  END IF;
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'major_name') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `major_name` VARCHAR(100) NULL DEFAULT NULL AFTER `major_no`;
  END IF;
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'class_scope') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `class_scope` VARCHAR(255) NULL DEFAULT NULL COMMENT '考试可见班级（中文列表）' AFTER `major_name`;
  END IF;
END$$
DELIMITER ;

-- Execute the procedure to ensure columns exist, then drop it
CALL add_missing_test_columns();
DROP PROCEDURE IF EXISTS add_missing_test_columns;

-- ==============================
-- End of idempotent migrations

/*
  Idempotent migration: ensure `test` table has additional columns required by mappers:
    - exam_intro LONGTEXT
    - camera_required TINYINT(1)
  This block creates a stored procedure that checks INFORMATION_SCHEMA and only alters the table
  when the column does not exist. It is safe to run multiple times.
*/
DELIMITER $$
DROP PROCEDURE IF EXISTS add_test_columns$$
CREATE PROCEDURE add_test_columns()
BEGIN
  IF NOT EXISTS(
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'exam_intro'
  ) THEN
    ALTER TABLE `test` ADD COLUMN `exam_intro` LONGTEXT NULL AFTER `exam_notice`;
  END IF;

  IF NOT EXISTS(
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'camera_required'
  ) THEN
    ALTER TABLE `test` ADD COLUMN `camera_required` TINYINT(1) NULL DEFAULT 0 AFTER `exam_intro`;
  END IF;

  IF NOT EXISTS(
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test' AND COLUMN_NAME = 'class_scope'
  ) THEN
    ALTER TABLE `test` ADD COLUMN `class_scope` VARCHAR(500) NULL DEFAULT NULL COMMENT '可见班级（中文描述）' AFTER `camera_required`;
  END IF;
END$$
CALL add_test_columns$$
DROP PROCEDURE IF EXISTS add_test_columns$$
DELIMITER ;

-- End of idempotent migration

/* ------------------------------------------------------------------
   Idempotent migrations: add missing columns referenced by code
   - Adds `exam_intro`, `exam_notice`, `camera_required` to `test` if absent
   - Adds `user_no` to auth for 12-digit external id (student/teacher/admin code)
   This block is safe to run multiple times; it checks INFORMATION_SCHEMA
   before altering any table to avoid Duplicate column errors.
   Usage: run this SQL in your `gp` database (mysql client / workbench).
   ------------------------------------------------------------------ */

DELIMITER $$
DROP PROCEDURE IF EXISTS add_missing_test_and_auth_columns$$
CREATE PROCEDURE add_missing_test_and_auth_columns()
BEGIN
  -- add exam_intro
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE table_schema = DATABASE() AND table_name = 'test' AND column_name = 'exam_intro') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `exam_intro` TEXT NULL COMMENT '考试介绍/试卷说明' AFTER `exam_notice`;
  END IF;

  -- add exam_notice
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE table_schema = DATABASE() AND table_name = 'test' AND column_name = 'exam_notice') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `exam_notice` TEXT NULL COMMENT '考试注意事项' AFTER `test_time`;
  END IF;

  -- add camera_required
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE table_schema = DATABASE() AND table_name = 'test' AND column_name = 'camera_required') = 0 THEN
    ALTER TABLE `test` ADD COLUMN `camera_required` TINYINT(1) NULL DEFAULT 0 COMMENT '是否强制摄像头(0/1)' AFTER `exam_intro`;
  END IF;

  -- add user_no to auth for 12-digit external id (student/teacher/admin code)
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE table_schema = DATABASE() AND table_name = 'auth' AND column_name = 'user_no') = 0 THEN
    ALTER TABLE `auth` ADD COLUMN `user_no` VARCHAR(50) NULL COMMENT '12位用户编号（学号/教工号/管理员编号）' AFTER `username`;
  END IF;

  -- Optionally, ensure role_name exists (some older schemas may lack it)
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE table_schema = DATABASE() AND table_name = 'auth' AND column_name = 'role_name') = 0 THEN
    ALTER TABLE `auth` ADD COLUMN `role_name` VARCHAR(50) NULL DEFAULT NULL COMMENT '角色名称（管理员/教师/学生）' AFTER `roleId`;
  END IF;

END$$
DELIMITER ;

-- Execute the procedure once (safe / idempotent)
CALL add_missing_test_and_auth_columns();

-- cleanup
DROP PROCEDURE IF EXISTS add_missing_test_and_auth_columns;

-- =====================================================================
-- Runtime fixes + 大批量样例数据（题目管理/阅卷管理联调）
-- =====================================================================

-- 1) 缺失表补齐：test_question / im_message（幂等）
DROP PROCEDURE IF EXISTS ensure_runtime_tables;
DELIMITER $$
CREATE PROCEDURE ensure_runtime_tables()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test_question'
  ) THEN
    CREATE TABLE `test_question` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `test_no` bigint(20) NOT NULL,
      `question_no` bigint(20) NOT NULL,
      `score` int(11) NOT NULL,
      `question_type` char(1) NOT NULL,
      `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`) USING BTREE,
      KEY `idx_test_no` (`test_no`) USING BTREE,
      KEY `idx_question_no` (`question_no`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'im_message'
  ) THEN
    CREATE TABLE `im_message` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `sender_no` varchar(50) NOT NULL,
      `sender_name` varchar(100) NOT NULL,
      `content` text NOT NULL,
      `target` varchar(50) DEFAULT 'all',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`) USING BTREE,
      KEY `idx_im_message_time` (`create_time`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'score_detail' AND COLUMN_NAME = 'grader_no'
  ) THEN
    ALTER TABLE `score_detail` ADD COLUMN `grader_no` varchar(50) NULL DEFAULT NULL COMMENT '认领批改教师账号' AFTER `student_answer`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'score_detail' AND COLUMN_NAME = 'locked_at'
  ) THEN
    ALTER TABLE `score_detail` ADD COLUMN `locked_at` datetime(0) NULL DEFAULT NULL COMMENT '认领时间' AFTER `grader_no`;
  END IF;
END$$
DELIMITER ;
CALL ensure_runtime_tables();
DROP PROCEDURE IF EXISTS ensure_runtime_tables;

-- 2) 大量客观题（单选/判断/多选）
INSERT INTO `choice` (`question`,`choice_a`,`choice_b`,`choice_c`,`choice_d`,`answer`,`course_name`,`type`,`create_time`,`update_time`)
SELECT CONCAT('Java单选题-', n), 'A. JVM','B. JRE','C. JDK','D. JIT',
       CASE (n % 4) WHEN 0 THEN 'A' WHEN 1 THEN 'B' WHEN 2 THEN 'C' ELSE 'D' END,
       'Java语言程序设计','1',NOW(),NOW()
FROM (
  SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
  SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
  SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL
  SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL
  SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL
  SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
) t;

INSERT INTO `choice` (`question`,`choice_a`,`choice_b`,`choice_c`,`choice_d`,`answer`,`course_name`,`type`,`create_time`,`update_time`)
SELECT CONCAT('数据库判断题-', n), '对','错','','', IF(n % 2 = 0,'A','B'), '数据库系统原理','2',NOW(),NOW()
FROM (
  SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
  SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
  SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL
  SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL
  SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
) t;

INSERT INTO `choice` (`question`,`choice_a`,`choice_b`,`choice_c`,`choice_d`,`answer`,`course_name`,`type`,`create_time`,`update_time`)
SELECT CONCAT('网络多选题-', n), 'A. TCP','B. UDP','C. IP','D. ARP',
       CASE (n % 3) WHEN 0 THEN 'ABC' WHEN 1 THEN 'ABD' ELSE 'ACD' END,
       '计算机网络基础','3',NOW(),NOW()
FROM (
  SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
  SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
  SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL
  SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL
  SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
) t;

-- 3) 大量主观题
INSERT INTO `subjective` (`question`,`answer`,`course_name`,`type`,`create_time`,`update_time`)
SELECT CONCAT('主观题-软件工程设计模式-', n),
       CONCAT('参考要点：定义、适用场景、优缺点、示例代码（', n, '）'),
       'Java语言程序设计','4',NOW(),NOW()
FROM (
  SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
  SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
  SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL
  SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL
  SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL
  SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
) t;

-- 4) 题目挂接到考试（解决考试预览无题）
DELETE FROM `test_question` WHERE `test_no` IN (202650010101,202650010102,202650010103,202650020101,202650020102);

INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650010101, id, 2, '1', NOW() FROM `choice` WHERE `type`='1' ORDER BY id DESC LIMIT 20;
INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650010101, id, 2, '2', NOW() FROM `choice` WHERE `type`='2' ORDER BY id DESC LIMIT 10;
INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650010101, id, 3, '3', NOW() FROM `choice` WHERE `type`='3' ORDER BY id DESC LIMIT 10;
INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650010101, id, 10, '4', NOW() FROM `subjective` ORDER BY id DESC LIMIT 4;

INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650010102, id, 2, '1', NOW() FROM `choice` WHERE `type`='1' ORDER BY id DESC LIMIT 10;
INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650010102, id, 5, '4', NOW() FROM `subjective` ORDER BY id DESC LIMIT 2;

INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650020101, id, 2, '2', NOW() FROM `choice` WHERE `type`='2' ORDER BY id DESC LIMIT 15;
INSERT INTO `test_question` (`test_no`,`question_no`,`score`,`question_type`,`create_time`)
SELECT 202650020101, id, 4, '4', NOW() FROM `subjective` ORDER BY id DESC LIMIT 3;

-- 5) paper_status（解决阅卷管理列表无数据）
DELETE FROM `paper_status` WHERE `test_no` IN (202650010101,202650010102,202650020101);
INSERT INTO `paper_status` (`test_no`,`test_name`,`choice_status`,`subjective_status`) VALUES
(202650010101,'Java期中考试','0','0'),
(202650010102,'Java 随堂测验一','0','0'),
(202650020101,'数据库系统 单元测试','0','0');

-- 6) score + score_detail（解决试卷答案查看/客观题批改/手动批改无数据）
DELETE FROM `score` WHERE `test_no` IN (202650010101,202650010102,202650020101);
DELETE FROM `score_detail` WHERE `test_no` IN (202650010101,202650010102,202650020101);

INSERT INTO `score` (`student_no`,`test_no`,`choice`,`subjective`,`total`,`create_time`) VALUES
('202630010101',202650010101,0,0,0,NOW()),
('202630010102',202650010101,0,0,0,NOW()),
('202630010201',202650010101,0,0,0,NOW()),
('202630010103',202650010101,0,0,0,NOW()),
('202630020101',202650020101,0,0,0,NOW()),
('202630020201',202650020101,0,0,0,NOW());

INSERT INTO `score_detail` (`student_no`,`test_no`,`question_no`,`student_score`,`question_score`,`question_type`,`flag`,`create_time`,`student_answer`)
SELECT '202630010101',202650010101,tq.question_no,0,tq.score,tq.question_type,'0',NOW(),
       CASE tq.question_type WHEN '1' THEN 'A' WHEN '2' THEN 'B' WHEN '3' THEN 'ACD' ELSE NULL END
FROM `test_question` tq WHERE tq.test_no=202650010101 AND tq.question_type IN ('1','2','3');

INSERT INTO `score_detail` (`student_no`,`test_no`,`question_no`,`student_score`,`question_score`,`question_type`,`flag`,`create_time`,`student_answer`)
SELECT '202630010102',202650010101,tq.question_no,0,tq.score,tq.question_type,'0',NOW(),
       CASE tq.question_type WHEN '1' THEN 'B' WHEN '2' THEN 'A' WHEN '3' THEN 'ABD' ELSE NULL END
FROM `test_question` tq WHERE tq.test_no=202650010101 AND tq.question_type IN ('1','2','3');

INSERT INTO `score_detail` (`student_no`,`test_no`,`question_no`,`student_score`,`question_score`,`question_type`,`flag`,`create_time`,`student_answer`)
SELECT '202630010101',202650010101,tq.question_no,0,tq.score,'4','0',NOW(),CONCAT('学生202630010101主观作答-题', tq.question_no)
FROM `test_question` tq WHERE tq.test_no=202650010101 AND tq.question_type='4';

INSERT INTO `score_detail` (`student_no`,`test_no`,`question_no`,`student_score`,`question_score`,`question_type`,`flag`,`create_time`,`student_answer`)
SELECT '202630010102',202650010101,tq.question_no,0,tq.score,'4','0',NOW(),CONCAT('学生202630010102主观作答-题', tq.question_no)
FROM `test_question` tq WHERE tq.test_no=202650010101 AND tq.question_type='4';

INSERT INTO `score_detail` (`student_no`,`test_no`,`question_no`,`student_score`,`question_score`,`question_type`,`flag`,`create_time`,`student_answer`)
SELECT '202630020101',202650020101,tq.question_no,0,tq.score,'4','0',NOW(),CONCAT('学生202630020101主观作答-题', tq.question_no)
FROM `test_question` tq WHERE tq.test_no=202650020101 AND tq.question_type='4';

INSERT INTO `im_message` (`sender_no`,`sender_name`,`content`,`target`,`create_time`) VALUES
('202610000001','管理员','考试系统已完成批量初始化，可开始联调。','all',NOW()),
('201220010101','教师A','请同学们按时完成在线考试。','student',NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- End of idempotent migration block

/* ------------------------------------------------------------------
   Notes:
   - Run this script against database `gp` (USE gp;). It will add columns
     only if they are missing and will not fail if the columns already exist.
   - After running: ensure backend MyBatis mappings and Java entities reference
     `exam_intro`, `exam_notice`, `camera_required` and `user_no` where needed.
   - If you prefer modifications to entity types (e.g., using BIGINT for ids),
     that requires coordinated Java code changes — I can prepare those next.
   ------------------------------------------------------------------ */

