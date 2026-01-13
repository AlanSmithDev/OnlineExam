// Shared role utilities for backage frontend

function normalizeRoleValue(v) {
  if (v === undefined || v === null) return null;
  const s = String(v).trim();
  // allow '00','0','1','2' etc
  if (/^\d{1,2}$/.test(s)) {
    const n = parseInt(s, 10);
    if (n === 0) return '0';
    if (n === 1) return '1';
    if (n === 2) return '2';
    if (n === 10) return '0';
    if (n === 20) return '2';
    if (n === 30) return '1';
  }
  if (/^\d+$/.test(s)) {
    if (s === '0' || s === '1' || s === '2') return s;
    if (s.length >= 6) {
      const two = s.substr(4,2);
      if (two === '10') return '0';
      if (two === '20') return '2';
      if (two === '30') return '1';
    }
    const last = s.charAt(s.length - 1);
    if (['0','1','2'].includes(last)) return last;
  }
  const low = s.toLowerCase();
  if (low.includes('学生') || low.includes('student') || low === 'stu' || low === 's') return '1';
  if (low.includes('教师') || low.includes('teacher') || low === 't') return '2';
  if (low.includes('管理员') || low.includes('admin') || low === 'a') return '0';
  return null;
}

function deriveRoleIdFromUsername(username) {
  if (!username) return null;
  const digits = String(username).replace(/\D/g, '');

  // 12位账号格式：202620010101，第5-6位表示角色
  // 10=管理员, 20=教师, 30=学生
  if (digits.length === 12) {
    const two = digits.substr(4, 2);
    if (two === '10') return '0';
    if (two === '20') return '2';
    if (two === '30') return '1';
  }

  // 6位账号格式：202610，第5-6位表示角色
  if (digits.length === 6) {
    const two = digits.substr(4, 2);
    if (two === '10') return '0';
    if (two === '20') return '2';
    if (two === '30') return '1';
  }

  // 兼容其他长度的账号，尝试从第5-6位识别
  if (digits.length >= 6) {
    const two = digits.substr(4, 2);
    if (two === '10') return '0';
    if (two === '20') return '2';
    if (two === '30') return '1';
  }

  // 如果无法识别，返回null让调用方处理
  return null;
}

export { normalizeRoleValue, deriveRoleIdFromUsername };
