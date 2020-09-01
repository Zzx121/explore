package cn.edu.djtu.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.*;
import lombok.Data;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@ContentRowHeight(30)
@HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
@HeadFontStyle(fontHeightInPoints = 20)
@ContentStyle(fillPatternType =  FillPatternType.LESS_DOTS, fillForegroundColor = 16)
@ContentFontStyle(fontHeightInPoints = 18)
public class DemoData {
    @HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 14)
    @HeadFontStyle(fontHeightInPoints = 20)
    @ContentStyle(fillPatternType =  FillPatternType.LESS_DOTS, fillForegroundColor = 30)
    @ContentFontStyle(fontHeightInPoints = 40)
    @ExcelProperty({"主标题", "字符串标题"})
    @ColumnWidth(50)
    @ContentLoopMerge(eachRow = 2)
    private String title;
   
    @ExcelProperty("日期")
    @DateTimeFormat("yyyy/MM/dd HH:mm:ss")
    @ColumnWidth(20)
    private Date date;
    
    @ExcelProperty(converter = LocalDateTimeStringConverter.class)
    @DateTimeFormat("yyyy年MM月dd日 HH:mm:ss")
    @ColumnWidth(30)
    private LocalDateTime localDateTime;
   
    @ExcelProperty(converter = LocalDateStringConverter.class)
    @ColumnWidth(15)
    private LocalDate localDate;
    
    @ExcelProperty("数值")
    private Double doubleData;
    
    @ExcelIgnore
    private String ignore;

    
}
