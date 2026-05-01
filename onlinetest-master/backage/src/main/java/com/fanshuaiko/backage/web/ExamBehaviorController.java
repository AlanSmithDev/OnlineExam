package com.fanshuaiko.backage.web;

import com.fanshuaiko.backage.utils.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/examBehavior")
public class ExamBehaviorController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/report")
    public ResultData report(@RequestBody Map<String, Object> payload) {
        ensureTable();

        Object testNoObj = payload.get("testNo");
        Object studentNoObj = payload.get("studentNo");
        Object eventTypeObj = payload.get("eventType");
        Object extraInfoObj = payload.get("extraInfo");

        Long testNo = parseLong(testNoObj);
        String studentNo = studentNoObj == null ? "" : String.valueOf(studentNoObj);
        String eventType = eventTypeObj == null ? "UNKNOWN" : String.valueOf(eventTypeObj);
        String extraInfo = extraInfoObj == null ? "{}" : String.valueOf(extraInfoObj);

        if (testNo == null) {
            return ResultData.newResultData("-1", "testNo不能为空");
        }
        if (studentNo == null || studentNo.trim().isEmpty()) {
            return ResultData.newResultData("-1", "studentNo不能为空");
        }

        int updated = jdbcTemplate.update(
                "insert into exam_behavior (test_no, student_no, event_type, extra_info) values (?, ?, ?, ?)",
                testNo,
                studentNo,
                eventType,
                extraInfo
        );
        return ResultData.newSuccessResultData(updated);
    }

    @GetMapping("/list")
    public ResultData list(
            @RequestParam(value = "testNo", required = false) Long testNo,
            @RequestParam(value = "studentNo", required = false) String studentNo,
            @RequestParam(value = "eventType", required = false) String eventType
    ) {
        ensureTable();

        StringBuilder sql = new StringBuilder(
                "select id, test_no as testNo, student_no as studentNo, event_type as eventType, extra_info as extraInfo, create_time as createTime from exam_behavior where 1=1"
        );
        List<Object> args = new ArrayList<>();

        if (testNo != null) {
            sql.append(" and test_no = ?");
            args.add(testNo);
        }
        if (studentNo != null && !studentNo.trim().isEmpty()) {
            sql.append(" and student_no = ?");
            args.add(studentNo.trim());
        }
        if (eventType != null && !eventType.trim().isEmpty()) {
            sql.append(" and event_type = ?");
            args.add(eventType.trim());
        }

        sql.append(" order by create_time desc, id desc");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        return ResultData.newSuccessResultData(list);
    }

    private Long parseLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.valueOf(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private void ensureTable() {
        jdbcTemplate.execute("create table if not exists exam_behavior ("
                + "id bigint primary key auto_increment,"
                + "test_no bigint not null,"
                + "student_no varchar(64) not null,"
                + "event_type varchar(64) not null,"
                + "extra_info text null,"
                + "create_time timestamp not null default current_timestamp"
                + ")");
    }
}
