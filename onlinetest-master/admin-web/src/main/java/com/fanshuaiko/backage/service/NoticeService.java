package com.fanshuaiko.backage.service;

import com.fanshuaiko.backage.entity.Notice;
import com.fanshuaiko.backage.utils.ResultData;

public interface NoticeService {
    ResultData list(Integer targetRole, Integer limit);

    ResultData listRules(Integer limit);

    ResultData publish(Notice notice);

    ResultData publishRule(Notice notice);

    ResultData update(Notice notice);

    ResultData delete(Long id);
}
