package com.fanshuaiko.backage.web;

import com.fanshuaiko.backage.entity.Notice;
import com.fanshuaiko.backage.service.NoticeService;
import com.fanshuaiko.backage.utils.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    private static final int RULE_TARGET_ROLE = 9;

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    public ResultData list(@RequestParam(value = "targetRole", required = false) Integer targetRole,
                           @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            return noticeService.list(targetRole, limit);
        } catch (Exception e) {
            return ResultData.newResultData("1", "通知查询异常:" + e.getMessage());
        }
    }

    @GetMapping("/rules")
    public ResultData listRules(@RequestParam(value = "limit", required = false) Integer limit) {
        try {
            return noticeService.listRules(limit);
        } catch (Exception e) {
            return ResultData.newResultData("1", "规则查询异常:" + e.getMessage());
        }
    }

    @PostMapping("/publish")
    public ResultData publish(@RequestBody Notice notice) {
        try {
            return noticeService.publish(notice);
        } catch (Exception e) {
            return ResultData.newResultData("1", "发布通知异常:" + e.getMessage());
        }
    }

    @PostMapping("/publishRule")
    public ResultData publishRule(@RequestBody Notice notice) {
        try {
            notice.setTargetRole(RULE_TARGET_ROLE);
            return noticeService.publishRule(notice);
        } catch (Exception e) {
            return ResultData.newResultData("1", "发布规则异常:" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResultData update(@RequestBody Notice notice) {
        try {
            return noticeService.update(notice);
        } catch (Exception e) {
            return ResultData.newResultData("1", "更新通知异常:" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResultData delete(@PathVariable("id") Long id) {
        try {
            return noticeService.delete(id);
        } catch (Exception e) {
            return ResultData.newResultData("1", "删除通知异常:" + e.getMessage());
        }
    }
}