package com.fanshuaiko.backage.web;

import com.fanshuaiko.backage.dao.StudentDao;
import com.fanshuaiko.backage.dao.TeacherDao;
import com.fanshuaiko.backage.entity.Student;
import com.fanshuaiko.backage.entity.Teacher;
import com.fanshuaiko.backage.utils.ResultData;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private TeacherDao teacherDao;

    // 上传头像，保存为 BLOB
    @PostMapping("/uploadAvatar")
    public ResultData uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResultData.newResultData("1", "文件为空");
            }
            String original = file.getOriginalFilename();
            String mime = file.getContentType();
            byte[] bytes = file.getBytes();

            // username 从上下文中获取（支持 Shiro 或 Servlet Principal）
            String username = resolveUsername();
            if (username == null) {
                return ResultData.newResultData("1", "未获取到用户名");
            }

            // 决定是学生还是教师（根据 auth.flag） — 简单查 student first
            Student s = studentDao.selectByStudentNo(username);
            if (s != null) {
                int ok = studentDao.updateAvatarByStudentNo(username, bytes, original, mime);
                java.util.Map<String,Object> data = new java.util.HashMap<>();
                data.put("avatarPresent", ok>0);
                data.put("avatarMime", mime);
                return ResultData.newSuccessResultData(data);
            }
            Teacher t = teacherDao.selectByTeacherNo(username);
            if (t != null) {
                int ok = teacherDao.updateAvatarByTeacherNo(username, bytes, original, mime);
                java.util.Map<String,Object> data = new java.util.HashMap<>();
                data.put("avatarPresent", ok>0);
                data.put("avatarMime", mime);
                return ResultData.newSuccessResultData(data);
            }
            return ResultData.newResultData("1", "未找到用户");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResultData.newResultData("1", "上传失败");
        }
    }

    // 获取头像（返回二进制流）
    @GetMapping("/avatar/{username}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable("username") String username) {
        try {
            Student s = studentDao.selectByStudentNo(username);
            if (s != null && s.getAvatarBlob() != null) {
                String mime = s.getAvatarMime() == null ? MediaType.IMAGE_JPEG_VALUE : s.getAvatarMime();
                byte[] blob = s.getAvatarBlob();
                String etag = java.util.Base64.getEncoder().encodeToString(java.util.Arrays.copyOfRange(java.security.MessageDigest.getInstance("MD5").digest(blob),0,8));
                // check If-None-Match
                // Note: Request headers not available in ResponseEntity method params here; use RequestContextHolder
                String ifNoneMatch = org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
                        .getAttribute("IF_NONE_MATCH", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST) == null ? null : org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes().getAttribute("IF_NONE_MATCH", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST).toString();
                // fallback: read header from HttpServletRequest
                javax.servlet.http.HttpServletRequest req = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
                ifNoneMatch = req.getHeader("If-None-Match");
                if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
                    return ResponseEntity.status(304).eTag(etag).build();
                }
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mime).eTag(etag).cacheControl(org.springframework.http.CacheControl.maxAge(300, java.util.concurrent.TimeUnit.SECONDS)).body(blob);
            }
            Teacher t = teacherDao.selectByTeacherNo(username);
            if (t != null && t.getAvatarBlob() != null) {
                String mime = t.getAvatarMime() == null ? MediaType.IMAGE_JPEG_VALUE : t.getAvatarMime();
                byte[] blob = t.getAvatarBlob();
                String etag = java.util.Base64.getEncoder().encodeToString(java.util.Arrays.copyOfRange(java.security.MessageDigest.getInstance("MD5").digest(blob),0,8));
                javax.servlet.http.HttpServletRequest req = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
                String ifNoneMatch = req.getHeader("If-None-Match");
                if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
                    return ResponseEntity.status(304).eTag(etag).build();
                }
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mime).eTag(etag).cacheControl(org.springframework.http.CacheControl.maxAge(300, java.util.concurrent.TimeUnit.SECONDS)).body(blob);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // 获取当前登录用户信息
    @GetMapping("/me")
    public ResultData getProfile(@RequestParam(value = "username", required = false) String usernameParam) {
        try {
            String username = normalizeUsername(resolveUsername());
            if ((username == null || username.isEmpty()) && usernameParam != null && !usernameParam.trim().isEmpty()) {
                username = usernameParam.trim();
            }
            java.util.Map<String,Object> data = new java.util.HashMap<>();
            if (username == null || username.isEmpty()) {
                return ResultData.newResultData("1", "未登录");
            }
            data.put("username", username);
            // try fetch student
            Student s = studentDao.selectByStudentNo(username);
            if (s != null) {
                data.put("name", s.getName());
                data.put("classNo", s.getClassNo());
                data.put("className", s.getClassName());
                data.put("phone", s.getPhone());
                data.put("email", s.getEmail());
                data.put("collegeName", s.getCollegeName());
                data.put("majorName", s.getMajorName());
                data.put("hasAvatar", s.getAvatarBlob() != null && s.getAvatarBlob().length>0);
                data.put("roleId", "1");
                return ResultData.newSuccessResultData(data);
            }
            Teacher t = teacherDao.selectByTeacherNo(username);
            if (t != null) {
                data.put("name", t.getName());
                data.put("title", "教师");
                data.put("phone", t.getPhone());
                data.put("email", t.getEmail());
                data.put("collegeName", t.getCollegeName());
                data.put("majorName", t.getMajorName());
                data.put("hasAvatar", t.getAvatarBlob() != null && t.getAvatarBlob().length>0);
                data.put("roleId", "2");
                return ResultData.newSuccessResultData(data);
            }
            // fallback: derive admin/student/teacher title from 12-digit username rules
            String roleId = null;
            String title = null;
            if (username != null && username.length() >= 6) {
                String two = username.substring(4, 6);
                if ("10".equals(two)) {
                    roleId = "0";
                    title = "管理员";
                } else if ("20".equals(two)) {
                    roleId = "2";
                    title = "教师";
                } else if ("30".equals(two)) {
                    roleId = "1";
                    title = "学生";
                }
            }
            data.put("name", username);
            data.put("title", title == null ? "未知职称" : title);
            data.put("hasAvatar", false);
            if (roleId != null) {
                data.put("roleId", roleId);
            }
            return ResultData.newSuccessResultData(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultData.newResultData("1", "查询失败");
        }
    }

    // 学生个人信息更新
    @PostMapping("/update")
    public ResultData updateProfile(@RequestBody java.util.Map<String, Object> payload) {
        try {
            String username = null;
            if (payload != null && payload.get("studentNo") != null) {
                username = String.valueOf(payload.get("studentNo")).trim();
            }
            if (username == null || username.isEmpty()) {
                username = normalizeUsername(resolveUsername());
            }
            if (username == null || username.isEmpty()) {
                return ResultData.newResultData("1", "未获取到学号");
            }
            Student s = studentDao.selectByStudentNo(username);
            if (s == null) {
                return ResultData.newResultData("1", "未找到学生信息");
            }
            String name = payload != null && payload.get("name") != null ? String.valueOf(payload.get("name")).trim() : s.getName();
            String classNo = payload != null && payload.get("classNo") != null ? String.valueOf(payload.get("classNo")).trim() : s.getClassNo();
            String className = payload != null && payload.get("className") != null ? String.valueOf(payload.get("className")).trim() : s.getClassName();
            String phone = payload != null && payload.get("phone") != null ? String.valueOf(payload.get("phone")).trim() : s.getPhone();
            String email = payload != null && payload.get("email") != null ? String.valueOf(payload.get("email")).trim() : s.getEmail();

            if (className == null || className.isEmpty()) {
                className = classNo;
            }
            if (classNo == null || classNo.isEmpty()) {
                classNo = className;
            }
            int updated = studentDao.updateProfileByStudentNo(username, name, classNo, className, phone, email);
            if (updated > 0) {
                return ResultData.newSuccessResultData(updated);
            }
            return ResultData.newResultData("1", "保存失败");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultData.newResultData("1", "保存失败");
        }
    }

    private String normalizeUsername(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim();
        int idx = v.indexOf(':');
        if (idx > -1 && idx < v.length() - 1) {
            return v.substring(idx + 1).trim();
        }
        return v;
    }

    private String resolveUsername() {
        try {
            Subject subject = SecurityUtils.getSubject();
            if (subject != null && subject.getPrincipal() != null) {
                return normalizeUsername(subject.getPrincipal().toString());
            }
        } catch (UnavailableSecurityManagerException ex) {
            log.debug("Shiro security manager unavailable, fallback to servlet principal", ex);
        } catch (Exception ignore) {
            log.debug("Failed to read principal from Shiro subject", ignore);
        }
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            if (request != null) {
                Principal servletPrincipal = request.getUserPrincipal();
                if (servletPrincipal != null) {
                    return normalizeUsername(servletPrincipal.getName());
                }
                Object attrUser = request.getAttribute("currentUser");
                if (attrUser != null) {
                    return normalizeUsername(attrUser.toString());
                }
            }
        }
        return null;
    }
}
