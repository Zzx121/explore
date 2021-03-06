package cn.edu.djtu.excel.util.poi;

import cn.edu.djtu.excel.common.annotation.Excel;
import cn.edu.djtu.excel.common.annotation.Excels;
import cn.edu.djtu.excel.common.application.ApplicationUtil;
import cn.edu.djtu.excel.common.property.ApplicationProperty;
import cn.edu.djtu.excel.util.basic.DateUtils;
import cn.edu.djtu.excel.util.basic.Convert;
import cn.edu.djtu.excel.util.basic.StringUtil;
import cn.edu.djtu.excel.util.reflect.ReflectUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

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

    /**
     * 对应关系Map中key对应导出到excel的值，value对应要导出的值
     */
    private Map<String, Map<String, Object>> expKeyMap;

    String basePath;
    String excelPath;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }
    

    /**
     * 需要动态传递转换对应关系时调用
     * @param clazz 实体类Class
     * @param expKeyMap 表达式对应Map
     */
    public ExcelUtil(Class<T> clazz, Map<String, Map<String, Object>> expKeyMap) {
        this.clazz = clazz;
        this.expKeyMap = expKeyMap;
    }

    public void init(List<T> list, String sheetName, Excel.Type type) {
        if (list == null) {
            list = new ArrayList<>();
        }
        this.list = list;
        this.sheetName = sheetName;
        this.type = type;
        parseExcelFields();
        createWorkbook();
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param list      导出数据集合
     * @param sheetName 工作表的名称
     * @return 结果
     */
    public String exportExcel(List<T> list, String sheetName) {
        this.init(list, sheetName, Excel.Type.EXPORT);
        return exportExcel();
    }

    /**
     * 导出Excel
     * @return 文件名
     */
    public String exportExcel() {
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
                try {
                    fillExcelData(i);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        String fileName = encodingFilename(sheetName);
        OutputStream out = null;
        try {
            out = new FileOutputStream(getAbsoluteFile(fileName));
            wb.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                wb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }
    
    private String getAbsoluteFile(String filename) {
        ApplicationProperty property = ApplicationUtil.getBean(ApplicationProperty.class);
        Path path = Paths.get(property.getBasePath(), property.getExcelPath(), filename);
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return path.toString();
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
            String expKey = attr.readConverterKey();
            String dateFormat = attr.dateFormat();
            //处理日期格式
            if (StringUtils.isNotBlank(dateFormat) && value != null) {
                if (value instanceof Date) {
                    cell.setCellValue(DateUtils.parseDateToStr(dateFormat, (Date) value));
                } else if (value instanceof LocalDate) {
                    cell.setCellValue(((LocalDate) value).format(DateTimeFormatter.ofPattern(dateFormat)));
                } else if (value instanceof LocalDateTime) {
                    cell.setCellValue(((LocalDateTime) value).format(DateTimeFormatter.ofPattern(dateFormat)));
                }
                //处理表达式对应
            } else if (StringUtils.isNotBlank(exp) && value != null) {
                cell.setCellValue(convertByExp(String.valueOf(value), exp, false));
                //处理动态传递过来的对应关系
            } else if (StringUtils.isNotBlank(expKey) && value != null) {
                cell.setCellValue(convertByExpMap(value, expKey, false));
            } else {
                cell.setCellValue(value == null ? attr.defaultValue() : (value + attr.suffix()));
            }
        }
    }

    /**
     * 解析导出值 如：0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param exp  翻译注解
     * @param isReverse false时为导出时使用，true时为导入时使用
     * @return 解析后值
     */
    private String convertByExp(String propertyValue, String exp, boolean isReverse) {
        String[] expArr = exp.split(",");
        for (String expItem : expArr) {
            String[] expItemPair = expItem.split("=");
            if (isReverse) {
                if (expItemPair[1].equals(propertyValue)) {
                    return expItemPair[0];
                }
            } else {
                if (expItemPair[0].equals(propertyValue)) {
                    return expItemPair[1];
                }
            }
        }
        
        return propertyValue;
    }

    /**
     * 表达式对应转换
     * @param propertyValue 属性值
     * @param expKey expKey
     * @param isReverse false时为导出时使用，true时为导入时使用
     * @return 根据expKey返回的对应值
     */
    private String convertByExpMap(Object propertyValue, String expKey, boolean isReverse) {
        if (expKeyMap != null) {
            Map<String, Object> expMap = expKeyMap.get(expKey);
            if (expMap != null) {
                for (Map.Entry<String, Object> expEntry : expMap.entrySet()) {
                    if (isReverse) {
                        if (propertyValue.equals(expEntry.getKey())) {
                            return String.valueOf(expEntry.getValue());
                        }
                    } else {
                        if (propertyValue.equals(expEntry.getValue())) {
                            return expEntry.getKey();
                        }
                    }
                }
            }
        }
        
        return propertyValue.toString();
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
        Predicate<Field> isExcelAnnotationPredicate = f -> f.isAnnotationPresent(Excel.class);
        tempFieldList.stream().filter(isExcelAnnotationPredicate)
        .forEach(f -> addToFields(f, f.getAnnotation(Excel.class)));
        tempFieldList.stream().filter(isExcelAnnotationPredicate)
                .forEach(f -> {
                    Excels excels = f.getAnnotation(Excels.class);
                    if (excels != null) {
                        Excel[] excelArr = excels.value();
                        for (Excel excel : excelArr) {
                            addToFields(f, excel);
                        }
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

    public Object getCellValue(Row row, int column) {
        if (row == null) {
            return null;
        }
        
        Object val = "";
        Cell cell = row.getCell(column);
        if (cell != null) {
            CellType cellType = cell.getCellType();
            if (cellType == CellType.NUMERIC || cellType == CellType.FORMULA) {
                val = cell.getNumericCellValue();
                if (DateUtil.isCellDateFormatted(cell)) {
                    val = DateUtil.getJavaDate((Double) val);
                } else {
                    if ((Double) val % 1 > 0) {
                        val = new DecimalFormat("0.00").format(val);
                    } else {
                        val = new DecimalFormat("0").format(val);
                    }
                }
            } else if (cellType == CellType.STRING) {
                val = cell.getStringCellValue();
            } else if (cellType == CellType.BOOLEAN) {
                val = cell.getBooleanCellValue();
            } else if (cellType == CellType.ERROR) {
                val = cell.getErrorCellValue();
            }
        }
        
        return val;
    }

    /**
     * 从excel文件导入数据
     * @param sheetName 指定的sheet名称
     * @param filePath 文件路径
     * @param headerRowIndex 表头所在行index
     * @return 从excel文件中解析出来的实体对象列表
     * @throws IOException exception
     */
    public List<T> importExcel(String sheetName, String filePath, int headerRowIndex) throws IOException {
        this.type = Excel.Type.IMPORT;
        this.wb = WorkbookFactory.create(new File(filePath));
        List<T> list = new ArrayList<>();
        Sheet sheet;
        if (StringUtils.isNotBlank(sheetName)) {
            sheet = wb.getSheet(sheetName);
        } else {
            sheet = wb.getSheetAt(0);
        }
        
        if (sheet == null) {
            throw new IOException("文件sheet不存在");
        }

        int rows = sheet.getPhysicalNumberOfRows();
        if (rows > 0) {
            Row headerRow = sheet.getRow(headerRowIndex);
            int cells = headerRow.getPhysicalNumberOfCells();
            this.parseExcelFields();
            List<String> excelHeaderList = new ArrayList<>();
            List<String> annotationHeaderList = parseAnnotationHeaderList();
            for (int i = 0; i < cells; i++) {
                excelHeaderList.add(String.valueOf(getCellValue(headerRow, i)));
            }
            //验证导入文件表头未被修改
            if (!annotationHeaderList.containsAll(excelHeaderList) || !excelHeaderList.containsAll(annotationHeaderList)) {
                throw new ExcelImportFileCheckException();
            }
            
            //按照导入文件表头顺序构建的Field list
            List<Field> fieldList = new ArrayList<>();
            excelHeaderList.forEach(s -> this.fields.forEach(m -> m.forEach((k, v) -> {
                if (s.equals(v.name())) {
                    fieldList.add(k);
                }
            })));
            
            //读取sheet内容
            for (int i = headerRowIndex + 1; i < rows; i++) {
                Row row = sheet.getRow(i);
                T entity = null;
                for (int j = 0; j < fieldList.size(); j++) {
                    Object cellValue = this.getCellValue(row, j);
                    Field field = fieldList.get(j);
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    try {
                       entity =  entity == null ? clazz.getDeclaredConstructor().newInstance() : entity;
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    // retrieve by type
                    if (String.class == type) {
                        cellValue = Convert.toStr(cellValue);
                    } else if (Integer.class == type || Integer.TYPE == type) {
                        cellValue = Convert.toInt(cellValue);
                    } else if (Long.class == type || Long.TYPE == type) {
                        cellValue = Convert.toLong(cellValue);
                    } else if (Double.class == type || Double.TYPE == type) {
                        cellValue = Convert.toDouble(cellValue);
                    } else if (Float.class == type || Float.TYPE == type) {
                        cellValue = Convert.toFloat(cellValue);
                    } else if (BigDecimal.class == type) {
                        cellValue = Convert.toBigDecimal(cellValue);
                    } else if (Date.class == type) {
                        if (cellValue instanceof String) {
                            cellValue = DateUtils.parseDate(cellValue);
                        } else if (cellValue instanceof Double) {
                            cellValue = DateUtil.getJavaDate((Double) cellValue);
                        }
                    } 
                    //TODO 处理LocalDate等
                    else if (LocalDate.class == type) {
                    }
                    
                    //对应值转换
                    Excel attr = field.getAnnotation(Excel.class);
                    String converterExp = attr.readConverterExp();
                    String converterKey = attr.readConverterKey();
                    if (StringUtil.isNotEmpty(converterExp)) {
                        cellValue = convertByExp(String.valueOf(cellValue), converterExp, true);
                    } else if (StringUtil.isNotEmpty(converterKey)) {
                        cellValue = convertByExpMap(cellValue, converterKey, true);
                        //移除后缀
                    } else {
                        String suffix = attr.suffix();
                        if (StringUtil.isNotEmpty(suffix) && StringUtil.isNotEmpty(cellValue)) {
                            String strVal = Convert.toStr(cellValue);
                            if (strVal.endsWith(suffix)) {
                                cellValue = strVal.substring(0, strVal.lastIndexOf(suffix));
                            }
                        }
                    }
                    ReflectUtil.invokeSetter(entity, field.getName(), cellValue);
                }
                
                list.add(entity);
            }
            deleteUsedFile(filePath);
        } else {
            deleteUsedFile(filePath);
            throw new ExcelImportFileCheckException();
        }
        
        return list;
    }
    
    private List<String> parseAnnotationHeaderList() {
        List<String> annotationHeaderList = new ArrayList<>();
        this.fields.forEach(m -> {
            m.forEach((k, v) -> {
                annotationHeaderList.add(v.name());
            });
        });
        
        return annotationHeaderList;
    }

    /**
     * 清除使用过的文件，如导入时保存的文件
     * @param path 文件路径
     */
    private void deleteUsedFile(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
