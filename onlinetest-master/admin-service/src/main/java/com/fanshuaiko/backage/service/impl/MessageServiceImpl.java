package com.fanshuaiko.backage.service.impl;

import com.fanshuaiko.backage.dao.ImMessageDao;
import com.fanshuaiko.backage.entity.ImMessage;
import com.fanshuaiko.backage.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ImMessageDao imMessageDao;

    @Override
    public void save(ImMessage message) {
        if (message == null) {
            return;
        }
        if (message.getCreateTime() == null) {
            message.setCreateTime(new Date());
        }
        if (message.getTarget() == null) {
            message.setTarget("ALL");
        }
        imMessageDao.insert(message);
    }

    @Override
    public List<ImMessage> latest(int limit) {
        int size = limit <= 0 ? 50 : Math.min(limit, 200);
        List<ImMessage> list = imMessageDao.selectLatest(size);
        if (list != null) {
            // 历史记录按时间升序显示
            java.util.Collections.reverse(list);
        }
        return list;
    }
}
