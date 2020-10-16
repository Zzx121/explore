package cn.edu.djtu.excel;

import cn.edu.djtu.excel.common.exception.ErrorCodeEnum;
import cn.edu.djtu.excel.entity.Customer;
import cn.edu.djtu.excel.entity.Gender;
import cn.edu.djtu.excel.service.ExcelReader;
import cn.edu.djtu.excel.util.poi.ExcelImportFileCheckException;
import cn.edu.djtu.excel.util.poi.ExcelUtil;
import com.google.common.base.MoreObjects;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.swing.text.Style;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.lang.System.out;

/**
 * @ClassName ExcelTest
 * @Description: TODO
 * @Author zzx
 * @Date 2020/4/22
 **/
public class ExcelTest {
    enum ReturnType {
        Gender, String, Integer, LocalDate;
    }
    int cnt = 0;

    @Test
    void readExcel() throws IOException, ExecutionException, InterruptedException {
        int threadCount = 4;
        long start = System.currentTimeMillis();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);
        ExecutorService reader = Executors.newSingleThreadExecutor();
        Future<List<Customer>> future = reader.submit(new ExcelReader());
        List<Customer> customers = future.get();
        long size = customers.size();
        long unitCount = size / threadCount;
        for (int i = 0; i < 2500; i++) {
            
        }
        for (int i = 2500; i < 5000; i++) {
            
        }
       
        long stop = System.currentTimeMillis();
        System.out.println(stop - start);
        ExecutorService executorService =  Executors.newFixedThreadPool(2);
        
    }
    
    @Test
    void readExcelTest() throws IOException {
        ExcelUtil<Customer> excelUtil = new ExcelUtil<>(Customer.class);
        List<Customer> list = excelUtil.importExcel(null, "customer.xlsx", 0);
        out.println(list.size());
    }
    
    @Test
    void writeExcel() {
        List<Customer> customerList = produceCustomers(10000);
        int listSize = customerList.size();
        LinkedHashMap<String, String> colOrderMap = new LinkedHashMap<>();
        colOrderMap.put("name", "name");
        colOrderMap.put("gender", "gender");
        colOrderMap.put("cellphone", "cellphone");
        colOrderMap.put("birthday", "birthday");
        colOrderMap.put("company", "company");
        colOrderMap.put("remarks", "remarks");
        
        long start = System.currentTimeMillis();
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();

        int threadCount = 4;
        int unitSize = listSize / threadCount;
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);
        
        createHeaderRow(wb, sheet, colOrderMap);
//        try {
//            renderSheet(wb, sheet, colOrderMap, customerList, 1);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
        // not faster than serial execute, but slower
        for (int t = 1; t <= threadCount; t++) {
            int finalT = t;
            service.submit(() -> {
                try {
                    int fromIndex = (finalT - 1) * unitSize;
                    int toIndex;
                    if (finalT == threadCount) {
                        toIndex = listSize - 1;
                    } else {
                        toIndex = finalT * unitSize;
                    }

                    List<Customer> subbedList = customerList.subList(fromIndex, toIndex);
                    renderSheet(wb, sheet, colOrderMap, Collections.synchronizedList(subbedList), fromIndex + 1);
                    doneSignal.countDown();
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                } 
            });
        }
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream out = new FileOutputStream("cus.xlsx");
            wb.write(out);
            
            out.close();
            wb.close();
            long stop = System.currentTimeMillis();
            System.out.println("total time:" + (stop - start));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    private void renderSheet(Workbook wb, Sheet sheet, LinkedHashMap<String, String> colOrderMap, 
                             List<Customer> customerList, int rowStart)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<String> keyList = new ArrayList<>(colOrderMap.keySet());
        
        Map<String, CellStyle> styles = createStyles(wb);

        Row row;
        Cell cell;
        int rowNum = rowStart;

        for (int i = 0; i < customerList.size(); i++, rowNum++) {
            row = createRow(sheet, rowNum);

            for (int j = 0; j < keyList.size(); j++) {
                cell = row.createCell(j);
                Customer c = customerList.get(i);
                Method getMethod = produceGetMethodByFieldName(c, keyList.get(j));
                String simpleReturnType = getMethod.getReturnType().getSimpleName();
                if (simpleReturnType.equals(ReturnType.Gender.toString())) {
                    Gender g = (Gender) getMethod.invoke(c);
                    cell.setCellValue(g.toString());
                } else if (simpleReturnType.equals(ReturnType.String.toString())) {
                    String s = (String) getMethod.invoke(c);
                    cell.setCellValue(s);
                } else if (simpleReturnType.equals(ReturnType.Integer.toString())) {
                    Integer intVal = (Integer) getMethod.invoke(c);
                    cell.setCellValue(intVal);
                } else if (simpleReturnType.equals(ReturnType.LocalDate.toString())) {
                    LocalDate date = (LocalDate) getMethod.invoke(c);
                    cell.setCellValue(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }

                cell.setCellStyle(styles.get("cell_b"));
            }
        }
    }
    
    private Row createRow(Sheet sheet, int rowNum) {
        return sheet.createRow(rowNum);
    }
    
    private void createHeaderRow(Workbook wb, Sheet sheet, LinkedHashMap<String, String> colOrderMap) {
        List<String> titleList = new ArrayList<>(colOrderMap.values());
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(12.75F);

        for (int i = 0; i < titleList.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titleList.get(i));
            cell.setCellStyle(createStyles(wb).get("header"));
        }
    }

    /**
     * create a library of cell styles
     */
    private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<>();
        DataFormat df = wb.createDataFormat();

        CellStyle style;
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        styles.put("header", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("header_date", style);

        Font font1 = wb.createFont();
        font1.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font1);
        styles.put("cell_b", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font1);
        styles.put("cell_b_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_b_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_g", style);

        Font font2 = wb.createFont();
        font2.setColor(IndexedColors.BLUE.getIndex());
        font2.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font2);
        styles.put("cell_bb", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_bg", style);

        Font font3 = wb.createFont();
        font3.setFontHeightInPoints((short)14);
        font3.setColor(IndexedColors.DARK_BLUE.getIndex());
        font3.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font3);
        style.setWrapText(true);
        styles.put("cell_h", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        styles.put("cell_normal", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        styles.put("cell_normal_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setWrapText(true);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_normal_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setIndention((short)1);
        style.setWrapText(true);
        styles.put("cell_indented", style);

        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_blue", style);

        return styles;
    }

    private static CellStyle createBorderedStyle(Workbook wb){
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();

        CellStyle style = wb.createCellStyle();
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderLeft(thin);
        style.setLeftBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);
        return style;
    }
    
    private String fieldToGetMethod(String field) {
        String getPrefix = "get";
        return getPrefix + field.substring(0, 1).toUpperCase() + field.substring(1);
    }
    
    private Method produceGetMethodByFieldName(Object o, String fieldName) throws NoSuchMethodException {
        return o.getClass().getMethod(fieldToGetMethod(fieldName));
    }
    
    private List<Customer> produceCustomers(int size) {
        List<Customer> customerList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Customer customer = new Customer();
            customer.setBirthday(LocalDate.of(1889,3,12));
            customer.setCellphone("18240853757");
            customer.setCompany("Google Company");
            customer.setGender(Gender.MALE);
            customer.setGmtCreate(LocalDateTime.now());
            customer.setIsDeleted(0);
            customer.setName("Tom Smith" + (i+1));
            customer.setRemarks("Work for 3 years");
            customerList.add(customer);
        }
        
        return customerList;
    }
    
    @Test
    void countWithoutLock() throws InterruptedException {
        Runnable runnable = () -> {
            int n = 10000;
            while(n > 0) {
                n--;
                cnt++;
            }
        };
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        Thread t3 = new Thread(runnable);
        Thread t4 = new Thread(runnable);
        Thread t5 = new Thread(runnable);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        
        Thread.sleep(10000);

        System.out.println(cnt);
    }
    
    @Test
    void countWithReEntrantLock() throws InterruptedException {
        ReentrantLock reentrantLock = new ReentrantLock();
        Runnable runnable = () -> {
            reentrantLock.lock();
            int n = 10000;
            while(n > 0) {
                n--;
                cnt++;
            }
            reentrantLock.unlock();
        };
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        Thread t3 = new Thread(runnable);
        Thread t4 = new Thread(runnable);
        Thread t5 = new Thread(runnable);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        
        Thread.sleep(10000);

        System.out.println(cnt);
    }
    
    @Test
    void divideTest() {
        int total = 100004;
        int perCount = total / 4;
        System.out.println(perCount + "-" + 2*perCount + "-" + 3*perCount);
    }
    
    @Test
    void reflectTest() throws ClassNotFoundException {
    }
    
    @Test
    void streamGroupByTest() {
        List<DemoData> demoDataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DemoData demoData = new DemoData();
            demoData.setIgnore(i % 2 == 0 ? "yes" : "no");
            demoData.setLocalDate(LocalDate.now());
            demoDataList.add(demoData);
        }

        Map<String, List<DemoData>> collect = demoDataList.stream().collect(Collectors.groupingBy(DemoData::getIgnore));
        out.println(collect);
    }
    
    @Test
    void listContainsTest() {
        List<Map<String, Long>> l1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Long> map = new HashMap<>();
            map.put("m" + i, (long) i);
            l1.add(map);
        }

        Map<String, Long> m1 = new HashMap<>();
        m1.put("m1", 1L);

        out.println(l1.contains(m1));
    }
    
    @Test
    void pathTest() throws IOException {
        String prefix = "/usr/file";
        String excelPath = "/excel";
        String filename = "234245253_user.xml";
        Path path = Paths.get(prefix, excelPath, filename);
        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
        }
        out.println(path.toString());
    }
    
    @Test
    void testBigDecimal() {
        BigDecimal a = BigDecimal.valueOf(123);
        BigDecimal b = BigDecimal.valueOf(13);
        out.println(b.multiply(BigDecimal.valueOf(100)).divide(a, 2, RoundingMode.HALF_UP));
        BigDecimal divide = a.divide(BigDecimal.ZERO, 2, RoundingMode.HALF_UP);
        out.println(divide);

    }
    
    @Test
    void listContainsAllTest() {
        List<String> l1 = Arrays.asList("A", "B", "C", "D");
        List<String> l2 = Arrays.asList("A", "B", "C", "D");
//        List<String> l2 = Arrays.asList("A", "C", "B", "D");
        out.println(l1.equals(l2));
    }
    
    @Test
    void enumTest() {
        out.println(ErrorCodeEnum.PHONE_IN_USE.msg());
    }
    
    @Test
    void charAtTest() {
        StringBuilder sb = new StringBuilder("12123(sdfa)/");
        int lastCharIndex = sb.length() - 1;
        if (sb.charAt(lastCharIndex) == '/') {
            sb.deleteCharAt(lastCharIndex);
        }

        out.println(sb);
    }
    
    @Test
    void streamTest() {
        List<Integer> integers = Arrays.asList(1, 2, 4, 5, 6, 7, 8, 9);
        integers.stream().filter(i -> i > 5).forEach(System.out::print);
        if (integers.size() > 3) {
            throw new ExcelImportFileCheckException();
        }
        out.println("---");
        integers.stream().filter(i -> i > 2).forEach(System.out::print);
    }
    
    @Test
    void listTest() {
        List<Integer> l1 = Arrays.asList(1, 2, 4, 5, 6, 7, 8, 9);
        List<Integer> l2 = Arrays.asList(1, 4, 2, 8, 6, 7, 5, 9);
        out.println(l1.containsAll(l2));
    }
    
    @Test
    void typeTest() {
        Class<Integer> a = int.class;
        Class<Integer> b = Integer.TYPE;
        Class<Integer> c = Integer.class;
        out.println(System.identityHashCode(a));
        out.println(System.identityHashCode(b));
        out.println(System.identityHashCode(c));
    }
    
    @Test
    void subStringTest() {
        String s = "abc%";
        String suffix = "%";
        String sub = s.substring(0, s.lastIndexOf(suffix));
        out.println(sub);
    }
    
    @Test
    void testStringBuilder() {
        StringBuilder sb = new StringBuilder();
        out.println(sb.toString().equals(""));
    }
    
    @Test
    void testMultiValueMap() {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("token", 128349);
        for (int i = 0; i < 8; i++) {
            map.add("count", i);
        }
        out.println(map);
        Map<String, Object> m = new HashMap<>();
        m.put("token", 12123);
        for (int i = 0; i < 10; i++) {
            m.put("count", i);
        }
        out.println(m);
    }
    
    @Test
    void streamToStringTest() {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        String s = integers.stream().map(Objects::toString).collect(Collectors.joining(","));
        Object[] objects = integers.stream().map(i -> i + "_" + "s").toArray();
        out.println(s);
        out.println(Arrays.toString(objects));
    }
    
    @Test
    void testBeanUtils() {
        List<Customer> customers = produceCustomers(10);
        customers.forEach(c -> {
            try {
                String name = (String) PropertyUtils.getSimpleProperty(c, "name");
                out.println(name);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }
}
