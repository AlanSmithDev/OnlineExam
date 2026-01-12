package com.fanshuaiko.backage.web;

import com.fanshuaiko.backage.entity.ImMessage;
import com.fanshuaiko.backage.service.MessageService;
import com.fanshuaiko.backage.utils.ErrorCode;
import com.fanshuaiko.backage.utils.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/history")
    public ResultData getHistory(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        try {
            List<ImMessage> list = messageService.latest(limit);
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, ErrorCode.QUERY_FAILOR_MSG);
        }
    }
}
