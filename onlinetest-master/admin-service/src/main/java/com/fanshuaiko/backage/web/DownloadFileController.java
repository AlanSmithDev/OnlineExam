package com.fanshuaiko.backage.web;

import com.fanshuaiko.backage.dao.ScoreDao;
import com.fanshuaiko.backage.entity.VO.ScoreQueryTerm;
import com.fanshuaiko.backage.entity.VO.ScoreReturnVo;
import com.fanshuaiko.backage.utils.ErrorCode;
import com.fanshuaiko.backage.utils.ExportUtil;
import com.fanshuaiko.backage.utils.ResultData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/file")
public class DownloadFileController {

    private static final Logger log = LoggerFactory.getLogger(DownloadFileController.class);

    private static final String NAME_SINGLE = "\u5355\u9009\u9898\u4e0a\u4f20\u6a21\u677f.xls";
    private static final String NAME_JUDGE = "\u5224\u65ad\u9898\u4e0a\u4f20\u6a21\u677f.xls";
    private static final String NAME_MULTIPLE = "\u591a\u9009\u9898\u4e0a\u4f20\u6a21\u677f.xls";
    private static final String NAME_SUBJECTIVE = "\u4e3b\u89c2\u9898\u4e0a\u4f20\u6a21\u677f.xls";

    @Autowired
    private ScoreDao scoreDao;

    @GetMapping("/single")
    public void downloadSingleChoiceTemplate(HttpServletResponse response) {
        downloadTemplate(response, NAME_SINGLE, new String[]{
                "\u9898\u76ee", "\u9009\u9879A", "\u9009\u9879B", "\u9009\u9879C", "\u9009\u9879D", "\u7b54\u6848", "\u6240\u5c5e\u8bfe\u7a0b"
        }, new String[]{
                "\u4ee5\u4e0b\u54ea\u4e2a\u662f\u5173\u7cfb\u578b\u6570\u636e\u5e93\uff1f", "MySQL", "Redis", "Nginx", "Kafka", "A", "\u6570\u636e\u5e93\u539f\u7406"
        });
    }

    @GetMapping("/judge")
    public void downloadJudgeChoiceTemplate(HttpServletResponse response) {
        downloadTemplate(response, NAME_JUDGE, new String[]{
                "\u9898\u76ee", "\u9009\u9879A", "\u9009\u9879B", "\u7b54\u6848", "\u6240\u5c5e\u8bfe\u7a0b"
        }, new String[]{
                "Java \u662f\u9762\u5411\u5bf9\u8c61\u8bed\u8a00\u3002", "\u6b63\u786e", "\u9519\u8bef", "A", "Java\u7a0b\u5e8f\u8bbe\u8ba1"
        });
    }

    @GetMapping("/multiple")
    public void downloadMultipleChoiceTemplate(HttpServletResponse response) {
        downloadTemplate(response, NAME_MULTIPLE, new String[]{
                "\u9898\u76ee", "\u9009\u9879A", "\u9009\u9879B", "\u9009\u9879C", "\u9009\u9879D", "\u7b54\u6848", "\u6240\u5c5e\u8bfe\u7a0b"
        }, new String[]{
                "\u4ee5\u4e0b\u54ea\u4e9b\u5c5e\u4e8e SQL \u5173\u952e\u5b57\uff1f", "SELECT", "FROM", "WHERE", "ORDER", "A,B,C,D", "\u6570\u636e\u5e93\u539f\u7406"
        });
    }

    @GetMapping("/subjective")
    public void downloadSubjectiveTemplate(HttpServletResponse response) {
        downloadTemplate(response, NAME_SUBJECTIVE, new String[]{
                "\u9898\u76ee", "\u7b54\u6848", "\u6240\u5c5e\u8bfe\u7a0b"
        }, new String[]{
                "\u8bf7\u7b80\u8ff0\u4e8b\u52a1\u7684 ACID \u7279\u6027\u3002",
                "\u539f\u5b50\u6027\u3001\u4e00\u81f4\u6027\u3001\u9694\u79bb\u6027\u3001\u6301\u4e45\u6027\u3002",
                "\u6570\u636e\u5e93\u539f\u7406"
        });
    }

    private void downloadTemplate(HttpServletResponse response, String fileName, String[] headers, String[] sampleRow) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("\u6a21\u677f");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                sheet.setColumnWidth(i, 22 * 256);
            }

            Row row1 = sheet.createRow(1);
            for (int i = 0; i < sampleRow.length; i++) {
                row1.createCell(i).setCellValue(sampleRow[i]);
            }

            String encodedName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedName);
            response.setHeader("filename", encodedName);
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("download template failed, fileName={}", fileName, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/score")
    public ResultData exportScoreExcel(ScoreQueryTerm scoreQueryTerm, HttpServletResponse response) {
        try {
            List<ScoreReturnVo> scoreReturnVoList = scoreDao.queryByConditions(scoreQueryTerm);
            if (scoreReturnVoList == null || scoreReturnVoList.isEmpty()) {
                return ResultData.newResultData(ErrorCode.FAILOR, "\u5bfc\u51fa\u6570\u636e\u4e3a\u7a7a");
            }
            ExportUtil.exportScoreExcel(response, scoreReturnVoList);
            return ResultData.newSuccessResultData();
        } catch (Exception e) {
            log.error("export score excel failed", e);
            return ResultData.newResultData(ErrorCode.FAILOR, "\u5bfc\u51fa\u5931\u8d25");
        }
    }
}

