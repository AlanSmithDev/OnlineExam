package com.fanshuaiko.backage.service.impl;

import com.fanshuaiko.backage.dao.NoticeDao;
import com.fanshuaiko.backage.entity.Notice;
import com.fanshuaiko.backage.service.NoticeService;
import com.fanshuaiko.backage.utils.ErrorCode;
import com.fanshuaiko.backage.utils.ResultData;
import com.fanshuaiko.backage.websocket.NoticeWebSocketEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NoticeServiceImpl implements NoticeService {

    private static final int RULE_TARGET_ROLE = 9;
    private static final AtomicLong FALLBACK_ID = new AtomicLong(900000000000L);
    private static final int FALLBACK_MAX_SIZE = 200;
    private static final LinkedList<Notice> FALLBACK_CACHE = new LinkedList<>();

    @Autowired
    private NoticeDao noticeDao;

    @Override
    public ResultData list(Integer targetRole, Integer limit) {
        try {
            List<Notice> list = noticeDao.listByTarget(targetRole, limit);
            if (list == null) {
                list = new ArrayList<>();
            }
            mergeFallbackByRole(list, targetRole, limit);
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            List<Notice> fallback = listFallbackByRole(targetRole, limit);
            return ResultData.newSuccessResultData(fallback);
        }
    }

    @Override
    public ResultData listRules(Integer limit) {
        try {
            List<Notice> list = noticeDao.listRules(limit);
            if (list == null) {
                list = new ArrayList<>();
            }
            mergeFallbackByRole(list, RULE_TARGET_ROLE, limit);
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            return ResultData.newSuccessResultData(listFallbackByRole(RULE_TARGET_ROLE, limit));
        }
    }

    @Override
    public ResultData publish(Notice notice) {
        return doPublish(notice, false);
    }

    @Override
    public ResultData publishRule(Notice notice) {
        return doPublish(notice, true);
    }

    @Override
    public ResultData update(Notice notice) {
        if (notice == null || notice.getId() == null) {
            return ResultData.newResultData(ErrorCode.UPDATE_FAILOR, "缺少通知ID");
        }
        if (!StringUtils.hasText(notice.getTitle()) || !StringUtils.hasText(notice.getContent())) {
            return ResultData.newResultData(ErrorCode.UPDATE_FAILOR, "标题、内容不能为空");
        }
        try {
            Notice old = noticeDao.getById(notice.getId());
            if (old == null) {
                old = findFallback(notice.getId());
            }
            if (old != null) {
                if (notice.getTargetRole() == null) notice.setTargetRole(old.getTargetRole());
                if (!StringUtils.hasText(notice.getPublisherNo())) notice.setPublisherNo(old.getPublisherNo());
                if (!StringUtils.hasText(notice.getPublisherName())) notice.setPublisherName(old.getPublisherName());
                if (!StringUtils.hasText(notice.getPublisherRoleName())) notice.setPublisherRoleName(old.getPublisherRoleName());
                if (notice.getCreateTime() == null) notice.setCreateTime(old.getCreateTime());
            }
            noticeDao.update(notice);
            replaceFallback(notice);
            NoticeWebSocketEndpoint.broadcastEvent("update", notice);
            return ResultData.newSuccessResultData(notice.getId());
        } catch (Exception e) {
            replaceFallback(notice);
            NoticeWebSocketEndpoint.broadcastEvent("update", notice);
            return ResultData.newSuccessResultData(notice.getId());
        }
    }

    @Override
    public ResultData delete(Long id) {
        if (id == null) {
            return ResultData.newResultData(ErrorCode.DEL_FAILOR, "缺少通知ID");
        }
        Notice deleted = null;
        try {
            deleted = noticeDao.getById(id);
            noticeDao.deleteById(id);
        } catch (Exception e) {
            // ignore and fallback
        }
        if (deleted == null) {
            deleted = removeFallback(id);
        } else {
            removeFallback(id);
        }
        if (deleted == null) {
            deleted = new Notice();
            deleted.setId(id);
            deleted.setTargetRole(0);
        }
        NoticeWebSocketEndpoint.broadcastEvent("delete", deleted);
        return ResultData.newSuccessResultData(id);
    }

    private ResultData doPublish(Notice notice, boolean isRule) {
        if (notice == null) {
            return ResultData.newResultData(ErrorCode.ADD_FAILOR, "参数不能为空");
        }
        if (!StringUtils.hasText(notice.getTitle()) || !StringUtils.hasText(notice.getContent())) {
            return ResultData.newResultData(ErrorCode.ADD_FAILOR, "标题、内容不能为空");
        }
        if (!StringUtils.hasText(notice.getPublisherNo()) || !StringUtils.hasText(notice.getPublisherName())) {
            return ResultData.newResultData(ErrorCode.ADD_FAILOR, "缺少发布人信息");
        }
        if (!StringUtils.hasText(notice.getPublisherRoleName())) {
            notice.setPublisherRoleName("系统");
        }
        if (isRule) {
            notice.setTargetRole(RULE_TARGET_ROLE);
        } else if (notice.getTargetRole() == null) {
            notice.setTargetRole(0);
        }
        if (notice.getCreateTime() == null) {
            notice.setCreateTime(new Date());
        }

        try {
            noticeDao.insert(notice);
        } catch (Exception e) {
            if (notice.getId() == null) {
                notice.setId(FALLBACK_ID.incrementAndGet());
            }
            addFallbackNotice(notice);
        }
        NoticeWebSocketEndpoint.broadcastEvent("create", notice);
        return ResultData.newSuccessResultData(notice.getId());
    }

    private void addFallbackNotice(Notice notice) {
        synchronized (FALLBACK_CACHE) {
            FALLBACK_CACHE.addFirst(cloneNotice(notice));
            while (FALLBACK_CACHE.size() > FALLBACK_MAX_SIZE) {
                FALLBACK_CACHE.removeLast();
            }
        }
    }

    private void replaceFallback(Notice notice) {
        synchronized (FALLBACK_CACHE) {
            removeFallbackInternal(notice.getId());
            FALLBACK_CACHE.addFirst(cloneNotice(notice));
            while (FALLBACK_CACHE.size() > FALLBACK_MAX_SIZE) {
                FALLBACK_CACHE.removeLast();
            }
        }
    }

    private Notice removeFallback(Long id) {
        synchronized (FALLBACK_CACHE) {
            return removeFallbackInternal(id);
        }
    }

    private Notice removeFallbackInternal(Long id) {
        for (int i = 0; i < FALLBACK_CACHE.size(); i++) {
            Notice n = FALLBACK_CACHE.get(i);
            if (id.equals(n.getId())) {
                return FALLBACK_CACHE.remove(i);
            }
        }
        return null;
    }

    private Notice findFallback(Long id) {
        synchronized (FALLBACK_CACHE) {
            for (Notice n : FALLBACK_CACHE) {
                if (id.equals(n.getId())) {
                    return cloneNotice(n);
                }
            }
        }
        return null;
    }

    private void mergeFallbackByRole(List<Notice> dbList, Integer targetRole, Integer limit) {
        List<Notice> fallback = listFallbackByRole(targetRole, limit);
        if (fallback.isEmpty()) {
            return;
        }
        dbList.addAll(0, fallback);
        if (limit != null && limit > 0 && dbList.size() > limit) {
            dbList.subList(limit, dbList.size()).clear();
        }
    }

    private List<Notice> listFallbackByRole(Integer targetRole, Integer limit) {
        int size = (limit == null || limit <= 0) ? 20 : Math.min(limit, 200);
        List<Notice> out = new ArrayList<>();
        synchronized (FALLBACK_CACHE) {
            for (Notice n : FALLBACK_CACHE) {
                if (matchRole(n.getTargetRole(), targetRole)) {
                    out.add(cloneNotice(n));
                    if (out.size() >= size) {
                        break;
                    }
                }
            }
        }
        return out;
    }

    private boolean matchRole(Integer noticeRole, Integer queryRole) {
        if (queryRole == null) {
            return true;
        }
        if (queryRole == RULE_TARGET_ROLE) {
            return queryRole.equals(noticeRole);
        }
        return noticeRole == null || noticeRole == 0 || queryRole.equals(noticeRole);
    }

    private Notice cloneNotice(Notice src) {
        Notice n = new Notice();
        n.setId(src.getId());
        n.setTitle(src.getTitle());
        n.setContent(src.getContent());
        n.setTargetRole(src.getTargetRole());
        n.setPublisherNo(src.getPublisherNo());
        n.setPublisherName(src.getPublisherName());
        n.setPublisherRoleName(src.getPublisherRoleName());
        n.setCreateTime(src.getCreateTime());
        return n;
    }
}
