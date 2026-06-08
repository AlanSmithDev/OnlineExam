package com.fanshuaiko.auth.web;

import com.fanshuaiko.auth.entity.Auth;
import com.fanshuaiko.auth.dao.UserProfileDao;
import com.fanshuaiko.auth.utils.ErrorCode;
import com.fanshuaiko.auth.utils.ResultData;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

import java.io.Serializable;

/**
 * @ClassName Login
 * @Description
 * @Author fanshuaiko
 * @Date 19-3-20 上午10:54
 * @Version 1.0
 **/
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${spring.redis.host:localhost}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private int port;
    @Value("${spring.redis.password:}")
    private String password;

    Logger logger = LoggerFactory.getLogger(AuthController.class);

    @org.springframework.beans.factory.annotation.Autowired
    private UserProfileDao userProfileDao;

    /** Shiro/Redis 下 sessionId 可能不是 String，强转会导致 500 */
    private static String sessionIdToString(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        if (sessionId instanceof String) {
            return (String) sessionId;
        }
        return String.valueOf(sessionId);
    }

    /**
     * 登录页默认账号提示（匿名可访问，与前端约定）
     */
    @GetMapping("/user/first")
    public ResultData firstDemoUser(
            @RequestParam(value = "portal", required = false, defaultValue = "admin") String portal) {
        java.util.Map<String, Object> data = new java.util.HashMap<>(2);
        if ("student".equalsIgnoreCase(portal)) {
            data.put("username", "202630010101");
        } else {
            // 管理端不再固定回填管理员账号，避免教师登录时被误导
            data.put("username", "");
        }
        return ResultData.newSuccessResultData(data);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResultData login() {
        // 经过 Shiro 过滤器后，认证已经在 Realm 中完成
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        String sessionId = sessionIdToString(session.getId());

        // Principal 在 MyShiroRealm 中设置为 Auth 对象
        Object principal = subject.getPrincipal();
        if (!(principal instanceof Auth)) {
            // 理论上不会发生，如果发生说明认证流程有问题
            return ResultData.newResultData(ErrorCode.FAILOR, "认证信息异常");
        }
        Auth auth = (Auth) principal;

        // 更健壮的角色判定：优先根据 teacher/student 表存在性判断；兼容 roleId/flag 标记
        String username = auth.getUsername();
        boolean isTeacher = false;
        boolean isStudent = false;
        boolean isAdmin = false;
        try {
            // Helper: normalize role-like strings to '0'/'1'/'2' or null
            java.util.function.Function<String, String> normRole = (s) -> {
                if (s == null) return null;
                s = s.trim();
                if (s.length() == 0) return null;
                // direct checks
                if (s.equalsIgnoreCase("0") || s.equals("00") || s.equalsIgnoreCase("admin")) return "0";
                if (s.equalsIgnoreCase("1") || s.equals("01") || s.equalsIgnoreCase("student") || s.contains("30")) return "1";
                if (s.equalsIgnoreCase("2") || s.equals("02") || s.equalsIgnoreCase("teacher") || s.contains("20")) return "2";
                // numeric parse
                try {
                    int n = Integer.parseInt(s);
                    if (n == 0) return "0";
                    if (n == 1) return "1";
                    if (n == 2) return "2";
                    if (n == 10) return "0";
                    if (n == 20) return "2";
                    if (n == 30) return "1";
                } catch (Exception ex) {
                    // ignore
                }
                return null;
            };

            String roleIdRaw = auth.getRoleId();
            String flagRaw = auth.getFlag();
            String rFromRoleId = normRole.apply(roleIdRaw);
            String rFromFlag = normRole.apply(flagRaw);
            String decided = null;
            if (rFromRoleId != null) decided = rFromRoleId;
            else if (rFromFlag != null) decided = rFromFlag;

            // Initial assignment based on decided value
            if (decided != null) {
                if (decided.equals("0")) isAdmin = true;
                else if (decided.equals("2")) isTeacher = true;
                else if (decided.equals("1")) isStudent = true;
            }

            // Try to use profile tables to confirm or override the decided role
            try {
                int t = userProfileDao.countTeacher(username);
                int s = userProfileDao.countStudent(username);
                // If DB shows teacher, prefer that (override accidental role flag mis-setting)
                if (t > 0) {
                    isTeacher = true;
                    isStudent = false;
                    isAdmin = false;
                }
                // If DB shows student and no teacher, prefer student
                else if (s > 0) {
                    isStudent = true;
                    isTeacher = false;
                    isAdmin = false;
                }
            } catch (Exception ex) {
                logger.debug("profile count check failed for {}: {}", username, ex.getMessage());
            }

            // If still unknown, fallback: infer from username digits (position 5-6)
            if (!isTeacher && !isStudent && !isAdmin) {
                if (username != null && username.length() >= 6) {
                    String two = username.substring(4,6);
                    if ("10".equals(two)) isAdmin = true;
                    else if ("20".equals(two)) isTeacher = true;
                    else if ("30".equals(two)) isStudent = true;
                }
            }

            // If still unknown, treat as admin only if roleId explicitly indicates admin; otherwise reject
            if (!isAdmin && !isTeacher && !isStudent) {
                if (rFromRoleId != null && rFromRoleId.equals("0")) {
                    isAdmin = true;
                } else {
                    subject.logout();
                    return ResultData.newResultData(ErrorCode.FAILOR, "账号角色无法识别，请联系管理员");
                }
            }

        } catch (Exception e) {
            logger.warn("Error when checking user profile existence", e);
            subject.logout();
            return ResultData.newResultData(ErrorCode.FAILOR, "账号错误");
        }

        // 统一返回 token + 最终判定角色信息，便于前端区分管理员/教师/学生
        String finalRoleId = isAdmin ? "0" : (isTeacher ? "2" : "1");
        java.util.Map<String, Object> data = new java.util.HashMap<>(4);
        data.put("token", sessionId);
        data.put("username", auth.getUsername());
        data.put("roleId", finalRoleId);
        data.put("flag", finalRoleId);

        return ResultData.newSuccessResultData(data);
    }

    /**
     * 注销
     */
    @PostMapping("/logout")
    public ResultData logout() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession();
            String sessionId = sessionIdToString(session.getId());
            logger.info("sessionId{}", sessionId);

            // 尝试删除 Redis 中的 session key；若 Redis 不可用则记录警告并返回成功
            try {
                JedisShardInfo shardInfo = new JedisShardInfo("redis://" + host + ":" + port);
                if (password != null && !password.isEmpty()) {
                    shardInfo.setPassword(password);
                }
                try (Jedis jedis = new Jedis(shardInfo)) {
                    jedis.del("shiro:session:" + sessionId);
                    logger.info("shiro session deleted from redis: {}", sessionId);
                }
            } catch (Exception e) {
                // Redis 不可达或操作失败 - 记录日志，但不要抛出异常
                logger.warn("Failed to delete session from Redis, proceeding without Redis cleanup", e);
            }

            // 最后登出 Subject
            try {
                Subject current = SecurityUtils.getSubject();
                if (current != null && current.isAuthenticated()) {
                    current.logout();
                }
            } catch (Exception ex) {
                logger.warn("Error during Shiro logout", ex);
            }

            return ResultData.newSuccessResultData();
        } catch (Exception e) {
            logger.info(e.getMessage());
            return ResultData.newResultData(ErrorCode.FAILOR, "注销失败！");
        }
    }

    @PostMapping("/test")
    public String test() {
        return "test";
    }
}
