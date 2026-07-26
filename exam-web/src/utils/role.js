// Utility helpers for role/ID parsing used by multiple front-end views.
// Provide both named exports and some compatibility shims for legacy code that may call
// `this.normalizeRoleValue` or expect globals.

import Vue from "vue";

function normalizeRoleValue(v) {
  if (v === undefined || v === null) return null;
  const s = String(v).trim();
  // Try short numeric roles like '0','1','2','00','01','02'
  if (/^\d{1,2}$/.test(s)) {
    const n = parseInt(s, 10);
    if (n === 0) return "0";
    if (n === 1) return "1";
    if (n === 2) return "2";
    // support legacy two-digit markers like 10/20/30
    if (n === 10) return "0";
    if (n === 20) return "2";
    if (n === 30) return "1";
  }
  // if purely numeric, try to interpret according to 12-digit id scheme
  if (/^\d+$/.test(s)) {
    // direct single-digit role codes
    if (s === "0" || s === "1" || s === "2") return s;
    // If string looks like a long id (>=6 digits), check position-based role code
    if (s.length >= 6) {
      // position 4-5 (0-based) holds two-digit role marker like '10'/'20'/'30'
      const two = s.substr(4, 2);
      if (two === "10") return "0";
      if (two === "20") return "2";
      if (two === "30") return "1";
    }
    // fallback: use last character if it's a role digit
    const last = s.charAt(s.length - 1);
    if (["0", "1", "2"].includes(last)) return last;
  }
  const low = s.toLowerCase();
  if (
    low.includes("student") ||
    low.includes("学生") ||
    low === "stu" ||
    low === "s"
  )
    return "1";
  if (low.includes("teacher") || low.includes("教师") || low === "t")
    return "2";
  if (low.includes("admin") || low.includes("管理员") || low === "a")
    return "0";
  return null;
}

function deriveRoleIdFromUsername(username) {
  if (!username) return null;
  const u = String(username).trim();
  const digits = u.replace(/\D/g, "");

  console.log(
    "[deriveRoleIdFromUsername] 原始账号:",
    username,
    "纯数字:",
    digits,
    "长度:",
    digits.length
  );

  if (digits.length < 4) {
    console.log("[deriveRoleIdFromUsername] 账号长度不足");
    return null;
  }

  // 检查第5-6位（角色编码）
  // 12位编号规则: YYYY RR CC MM SS
  // RR = 10 管理员, 20 教师, 30 学生
  if (digits.length >= 6) {
    const roleSeg = digits.substring(4, 6); // 第5-6位
    console.log("[deriveRoleIdFromUsername] 第5-6位角色编码:", roleSeg);
    if (roleSeg === "10") {
      console.log("[deriveRoleIdFromUsername] 识别为管理员");
      return "0";
    }
    if (roleSeg === "20") {
      console.log("[deriveRoleIdFromUsername] 识别为教师");
      return "2";
    }
    if (roleSeg === "30") {
      console.log("[deriveRoleIdFromUsername] 识别为学生");
      return "1";
    }
  }

  // 兼容处理：以30开头的是学生
  if (digits.startsWith("30")) {
    console.log("[deriveRoleIdFromUsername] 以30开头，识别为学生");
    return "1";
  }

  console.log("[deriveRoleIdFromUsername] 无法推导角色");
  return null;
}

// Compatibility: export named functions and default object
const RoleUtils = { normalizeRoleValue, deriveRoleIdFromUsername };

// Attach to Vue.prototype to support legacy components using this.normalizeRoleValue
try {
  if (Vue && Vue.prototype) {
    if (!Vue.prototype.normalizeRoleValue)
      Vue.prototype.normalizeRoleValue = normalizeRoleValue;
    if (!Vue.prototype.deriveRoleIdFromUsername)
      Vue.prototype.deriveRoleIdFromUsername = deriveRoleIdFromUsername;
  }
} catch (e) {
  // ignore
}

// Make functions available on global/window for legacy code that may expect them
if (typeof window !== "undefined") {
  try {
    window.normalizeRoleValue = normalizeRoleValue;
    window.deriveRoleIdFromUsername = deriveRoleIdFromUsername;
  } catch (e) {
    // ignore quietly
  }
}

export { normalizeRoleValue, deriveRoleIdFromUsername };
export default RoleUtils;
