package cn.edu.djtu.excel.util.poi;

import cn.edu.djtu.excel.common.annotation.Excel;
import cn.edu.djtu.excel.common.annotation.Excels;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Excel 处理类
 */
public class ExcelUtil<T> {
    /**
     * Excel sheet最大行数，默认65536
     */
    public static final int SHEET_SIZE = 65536;

    /**
     * 工作表名称
     */
    private String sheetName;

    /**
     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
     */
    private Excel.Type type;

    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * 导入导出数据列表
     */
    private List<T> list;

    /**
     * 注解列表
     */
    private List<Map<Field, Excel>> fields;

    /**
     * 实体对象
     */
    public final Class<T> clazz;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 导出Excel
     * @return 文件名
     */
    public String exportExcel() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //sheet数量
        int sheetCount = list.size() / SHEET_SIZE + 1;
        for (int i = 0; i < sheetCount; i++) {
            this.sheet = wb.createSheet();
            
            //渲染header
            Row row = sheet.createRow(0);
            int column = 0;
            for (Map<Field, Excel> m : fields) {
                renderHeaderCell(m.entrySet().iterator().next().getValue(), row, column++);
            }
            //渲染内容
            if (Excel.Type.EXPORT.equals(type)) {
                fillExcelData(i);
            }
        }
        String fileName = encodingFilename(sheetName);
        return null;
    }
    
    private void fillExcelData(int sheetIndex) throws IllegalAccessException, 
            NoSuchMethodException, InvocationTargetException {
        int startRowIndex = sheetIndex * SHEET_SIZE;
        int endRowIndex = Math.min(list.size(), startRowIndex + SHEET_SIZE);

        CellStyle cs = wb.createCellStyle();
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        for (int i = startRowIndex; i < endRowIndex; i++) {
            Row row = sheet.createRow(i + 1 - startRowIndex);
            T vo = list.get(i);
            int columnIndex = 0;
            for (Map<Field, Excel> field : fields) {
                Map.Entry<Field, Excel> entry = field.entrySet().iterator().next();
                Excel attr = entry.getValue();
                Field f = entry.getKey();
                
                f.setAccessible(true);
                renderCell(row, vo, f, attr, columnIndex++, cs);
            }
        }
    }
    
    private void renderCell(Row row, T vo, Field field, Excel attr, int columnIndex, CellStyle cs) 
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        row.setHeight((short) (attr.height() * 20));
        //对应值是否需要导出
        if (attr.isExport()) {
            Cell cell = row.createCell(columnIndex);
            cell.setCellStyle(cs);

            Object value = acquireValueByFieldGet(field, vo, attr);
            String exp = attr.readConverterExp();
            String dateFormat = attr.dateFormat();
            //处理日期格式
            if (StringUtils.isNotBlank(dateFormat) && value != null) {
                cell.setCellValue(DateUtils.parseDateToStr(dateFormat, (Date) value));
                //处理表达式对应
            } else if (StringUtils.isNotBlank(exp) && value != null) {
                cell.setCellValue(convertByExp(String.valueOf(value), exp));
            } else {
                cell.setCellValue(value == null ? attr.defaultValue() : (value + attr.suffix()));
            }
        }
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param exp  翻译注解
     * @return 解析后值
     */
    private String convertByExp(String propertyValue, String exp) {
        String[] expArr = exp.split(",");
        for (String expItem : expArr) {
            String[] expItemPair = expItem.split("=");
            if (expItemPair[0].equals(propertyValue)) {
                return expItemPair[1];
            }
        }
        
        return propertyValue;
    }
    
    private Object acquireValueByFieldGet(Field field, T vo, Excel attr) throws IllegalAccessException, 
            NoSuchMethodException, InvocationTargetException {
        Object o = field.get(vo);
        String targetAttr = attr.targetAttr();
        //处理嵌套或者关联数据
        if (StringUtils.isNotBlank(targetAttr)) {
            o = acquireValueByGetMethod(o, targetAttr);
        }
        
        return o;
    }
    
    private Object acquireValueByGetMethod(Object o, String fieldName) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class<?> clazz = o.getClass();
        String getMethodStr = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method method = clazz.getMethod(getMethodStr); 
        return method.invoke(o);
    }

    /**
     * 解析出(Field，Excel)列表
     */
    private void parseExcelFields() {
        this.fields = new ArrayList<>();
        List<Field> tempFieldList = new ArrayList<>();
        // In case of Entity's inheritance
        tempFieldList.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        tempFieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
        
        tempFieldList.stream().filter(field -> field.isAnnotationPresent(Excel.class))
        .forEach(f -> addToFields(f, f.getAnnotation(Excel.class)));
        tempFieldList.stream().filter(field -> field.isAnnotationPresent(Excels.class))
                .forEach(f -> {
                    Excel[] excelArr = f.getAnnotation(Excels.class).value();
                    for (Excel excel : excelArr) {
                        addToFields(f, excel);
                    }
                });
    }

    /**
     * 筛选出对应类型并加入到fields中
     */
    private void addToFields(Field field, Excel attr) {
        if (attr != null && (attr.type() == Excel.Type.ALL || attr.type() == type)) {
            Map<Field, Excel> fieldExcelMap = new HashMap<>(2);
            fieldExcelMap.put(field, attr);
            this.fields.add(fieldExcelMap);
        }
    }

    /**
     * 创建工作簿
     */
    private void createWorkbook() {
        this.wb = new SXSSFWorkbook(500);
    }

    /**
     * 表头单元格内容填充
     * @param attr 字段属性
     * @param row 当前行
     * @param column 当前列序号
     */
    private void renderHeaderCell(Excel attr, Row row, int column) {
        Cell cell = row.createCell(column);
        cell.setCellValue(attr.name());
        cell.setCellStyle(createHeaderCellStyle(attr, row, column));
    }

    /**
     * 表头单元格样式
     * @param attr 字段属性
     * @param row 当前行
     * @param column 当前列序号
     */
    private CellStyle createHeaderCellStyle(Excel attr, Row row, int column) {
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        // 粗体显示
        font.setBold(true);
        // 选择需要用到的字体格式
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(attr.headerForegroundColor());
        // 设置列宽
        sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
        row.setHeight((short) (attr.height() * 20));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setWrapText(true);
        // 如果设置了提示信息则鼠标放上去提示.
        if (StringUtils.isNotEmpty(attr.prompt())) {
            // 这里默认设了2-101列提示.
            setXSSFPrompt(sheet, "", attr.prompt(), 1, 100, column, column);
        }
        // 如果设置了combo属性则本列只能选择不能输入
        if (attr.combo().length > 0) {
            // 这里默认设了2-101列只能选择不能输入.
            setXSSFValidation(sheet, attr.combo(), 1, 100, column, column);
        }
        
        return cellStyle;
    }

    /**
     * 设置 POI XSSFSheet 单元格提示
     *
     * @param sheet         表单
     * @param promptTitle   提示标题
     * @param promptContent 提示内容
     * @param firstRow      开始行
     * @param endRow        结束行
     * @param firstCol      开始列
     * @param endCol        结束列
     */
    private void setXSSFPrompt(Sheet sheet, String promptTitle, String promptContent, int firstRow, int endRow,
                              int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createCustomConstraint("DD1");
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        dataValidation.createPromptBox(promptTitle, promptContent);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet    要设置的sheet.
     * @param textlist 下拉框显示的内容
     * @param firstRow 开始行
     * @param endRow   结束行
     * @param firstCol 开始列
     * @param endCol   结束列
     * @return 设置好的sheet.
     */
    private void setXSSFValidation(Sheet sheet, String[] textlist, int firstRow, int endRow, int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        // 加载下拉列表内容
        DataValidationConstraint constraint = helper.createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        // 处理Excel兼容性问题
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }

        sheet.addValidationData(dataValidation);
    }

    /**
     * 编码文件名
     */
    public String encodingFilename(String filename) {
        return filename + "_" + UUID.randomUUID().toString() + ".xlsx";
    }


}
