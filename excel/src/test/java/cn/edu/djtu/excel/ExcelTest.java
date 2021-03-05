package cn.edu.djtu.excel;

import cn.edu.djtu.excel.common.exception.ErrorCodeEnum;
import cn.edu.djtu.excel.entity.Customer;
import cn.edu.djtu.excel.entity.Gender;
import cn.edu.djtu.excel.service.ExcelReader;
import cn.edu.djtu.excel.util.poi.ExcelImportFileCheckException;
import cn.edu.djtu.excel.util.poi.ExcelUtil;
import com.diffplug.common.base.TreeNode;
import com.diffplug.common.base.TreeQuery;
import com.diffplug.common.base.TreeStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
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
    
    @Test
    void URITest() throws URISyntaxException {
        URIBuilder builder = new URIBuilder("/usr/local");
        builder.addParameter("name", "sam");
        builder.addParameter("pass", "sdk2");
        String s = builder.toString();
        out.println(s);
    }
    
    @Test
    void stringSuffix() {
        final String REQUIRED_BRACKET = "(*)";
        String s = "名称(*)";
        out.println(s.substring(0, s.length() -3));
    }
    
    @Test
    void continueBreakTest() {
        for (int i = 0; i < 10; i++) {
            if (i == 5) {
                break;
            }
            out.println(i);
        }
    }
    
    @Test
    void regexTest() {
//        boolean matches = Pattern.compile("^[a-zA-Z]$").matcher("w@").matches();
        boolean matches = Pattern.compile("^[\\u4e00-\\u9fa5]+$").matcher("你好").matches();
        out.println(matches);
    }
    
    @Test
    void splitTest() {
        String str = "客服部/一部/一分部";
        String[] split = str.split("/");
        out.println(split[split.length - 1]);
    }
    
    @Test
    void stringBuilderTest() {
        StringBuilder sb = new StringBuilder("just improve yourself!");
        sb.setLength(5);
        out.println(sb.toString());
    }
    
    @Test
    void treeTest() {
        
        String jsonTree = "[{\"organCode\":\"rdyy\",\"pid\":0,\"id\":4541840053501963,\"text\":\"睿丁英语责任有限公司\"}," +
                "{\"organCode\":\"\",\"pid\":4541840053501963,\"id\":4541841739612190,\"text\":\"客服123\"}," +
                "{\"organCode\":\"\",\"pid\":4541841739612190,\"id\":4541842020630585,\"text\":\"客服一部\"}," +
                "{\"organCode\":\"\",\"pid\":4541842020630585,\"id\":4545155256549386,\"text\":\"客服顾问\"}," +
                "{\"organCode\":\"\",\"pid\":4541842020630585,\"id\":4545474338226177,\"text\":\"顾问1\"}," +
                "{\"organCode\":\"\",\"pid\":4545474338226177,\"id\":4554507010965513,\"text\":\"A部门\"}," +
                "{\"organCode\":\"\",\"pid\":4545474338226177,\"id\":4561071805562887,\"text\":\"B部门\"}," +
                "{\"organCode\":\"\",\"pid\":4541841739612190,\"id\":4541842108710977,\"text\":\"客服三部\"}," +
                "{\"organCode\":\"\",\"pid\":4541842108710977,\"id\":4547970842492931,\"text\":\"顾问\"}," +
                "{\"organCode\":\"\",\"pid\":4541842108710977,\"id\":4562153432678415,\"text\":\"顾问1\"}," +
                "{\"organCode\":\"\",\"pid\":4541842108710977,\"id\":4563118630109199,\"text\":\"分组\"}," +
                "{\"organCode\":\"\",\"pid\":4541841739612190,\"id\":4563118202290181,\"text\":\"我是员工\"}," +
                "{\"organCode\":\"\",\"pid\":4541841739612190,\"id\":4563118227456011,\"text\":\"订单\"}," +
                "{\"organCode\":\"\",\"pid\":4541840053501963,\"id\":4542202646888453,\"text\":\"财务部1\"}]";
        
        Gson gson = new Gson();
        List<Map<String, Object>> jsonMapList = gson.fromJson(jsonTree, new TypeToken<List<Map<String, Object>>>() {}.
                getType());
        Optional<Map<String, Object>> rootOptional = jsonMapList.stream().filter(m -> String.valueOf(m.get("pid")).equals("0.0")).findFirst();
        TreeNode<Map<String, Object>> rootNode = new TreeNode<>(null, null);
        if (rootOptional.isPresent()) {
            rootNode =  new TreeNode<>(null, rootOptional.get());
            TreeNode<Map<String, Object>> lastNode = rootNode;
            TreeNode<Map<String, Object>> finalLastNode = lastNode;
            List<Map<String, Object>> childrenList = jsonMapList.stream().filter(m -> String.valueOf(m.get("pid")).
                    equals(String.valueOf(finalLastNode.getContent().get("id")))).collect(Collectors.toList());
            for (Map<String, Object> map : childrenList) {
                lastNode = new TreeNode<>(lastNode, map);
            }
        }
        out.println(rootNode.getPath());

    }
    
    private List<FlatTreeNode> generateNodes() {
        List<FlatTreeNode> nodes = new ArrayList<>();
        FlatTreeNode treeNode = FlatTreeNode.builder().content(generateRandomWord(4)).id(1L).parentId(0L).build();
        nodes.add(treeNode);
        treeNode = FlatTreeNode.builder().content(generateRandomWord(5)).id(2L).parentId(1L).build();
        nodes.add(treeNode);
        treeNode = FlatTreeNode.builder().content(generateRandomWord(6)).id(3L).parentId(1L).build();
        nodes.add(treeNode);
        treeNode = FlatTreeNode.builder().content(generateRandomWord(2)).id(4L).parentId(2L).build();
        nodes.add(treeNode);
        treeNode = FlatTreeNode.builder().content(generateRandomWord(3)).id(5L).parentId(2L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(9)).id(6L).parentId(3L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(5)).id(7L).parentId(3L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(7)).id(8L).parentId(4L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(6)).id(9L).parentId(5L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(4)).id(10L).parentId(7L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(8)).id(11L).parentId(8L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(6)).id(12L).parentId(10L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(5)).id(13L).parentId(10L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(1)).id(14L).parentId(12L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(7)).id(15L).parentId(13L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(9)).id(16L).parentId(13L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(8)).id(17L).parentId(15L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(12)).id(18L).parentId(16L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(10)).id(19L).parentId(16L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(9)).id(20L).parentId(16L).build();
        nodes.add(treeNode);

        treeNode = FlatTreeNode.builder().content(generateRandomWord(8)).id(21L).parentId(16L).build();
        nodes.add(treeNode);
        
        return nodes;
    }
    
    private FlatTreeNode getParentNode(List<FlatTreeNode> nodes) {
        return nodes.stream().filter(n -> n.getParentId() == null || n.getParentId().equals(0L)).findFirst().orElseThrow();
    }
    
    @Test
    void customerTreeTest() {
        List<FlatTreeNode> nodes = generateNodes();
        FlatTreeNode rootNode = getParentNode(nodes);
        TreeNode<FlatTreeNode> treeNode = new TreeNode<>(null, rootNode);
        
        helper(nodes, treeNode, rootNode);
        
        out.println(treeNode.getChildren().get(0).getChildren().get(0).getPath(a -> String.valueOf(a.getId())));
    }
    
    @Test
    void multiValueMapTest() {
        List<FlatTreeNode> nodes = generateNodes();
        MultiValueMap<String, FlatTreeNode> attrMap = new LinkedMultiValueMap<>();
        attrMap.put("nodes", nodes);
        out.println(attrMap);
    }
    
    private void helper(List<FlatTreeNode> nodes, TreeNode<FlatTreeNode> finalNode, FlatTreeNode parentNode) {
        List<FlatTreeNode> childrenNodes = getChildrenNodes(nodes, parentNode);
        if (childrenNodes.size() > 0) {
            for (FlatTreeNode childrenNode : childrenNodes) {
                helper(nodes, new TreeNode<>(finalNode, childrenNode), childrenNode);
            }
        }
    }
    
    private List<FlatTreeNode> getChildrenNodes(List<FlatTreeNode> nodes, FlatTreeNode parentNode) {
        return nodes.stream().filter(n -> parentNode.getId().equals(n.getParentId())).collect(Collectors.toList());
    }
    
    @Test
    void streamCollectionTest() {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> collect = integers.stream().filter(i -> i > 6).collect(Collectors.toList());
        out.println(collect.size());
    }
    
    private String generateRandomWord(int length) {
        final String letterPool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int len = letterPool.length();
        Random r = new Random();
        
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < length; j++) {
            int i = r.nextInt(len - 1);
            stringBuilder.append(letterPool.charAt(i));
        }
        
        return stringBuilder.toString();
    }
    
    @Test
    void stringSubTest() {
        String extendPrefix = "extend";
        String s = "extend2";
        out.println(s.substring(extendPrefix.length()));
    }
    
    @Test
    void collectionRemoveTest() {
        List<Integer> fiveIntegers = Arrays.asList(1, 4, 5);
        List<Integer> eightIntegers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        List<Integer> collect = eightIntegers.stream().filter(i -> !fiveIntegers.contains(i)).collect(Collectors.toList());
        Integer suffixNum = collect.stream().min(Integer::compare).get();
        out.println(collect);
        out.println(suffixNum);
    }
    
    private TreeNode<String> testData = TreeNode.createTestData("root", " A", "  B", "   C", 
            " 1", "  2", "   3", "   a");
    
    private TreeNode<String> getNode(String name) {
        return testData.findByContent(name);
    }
    
    @Test
    void trimTest() {
        String s = " 天 津 天  津";
        out.println(s.replaceAll("\\s+", ""));
    }
    
    @Test
    void testMoreObjects() {
        out.println(MoreObjects.firstNonNull("SAM", "Hello"));
        out.println(MoreObjects.firstNonNull(null, "Hello"));
    }
    
    
    @Test
    void listIterateAndSet() {
        List<FlatTreeNode> nodes = generateNodes();
        out.println(nodes.size());
        FlatTreeNode rootNode = nodes.get(0);
        rootNode.setPath(String.valueOf(rootNode.getId()));
        rootNode.setNamePath(rootNode.getContent());
        nodes.forEach(n -> {
            Long id = n.getId();
            List<FlatTreeNode> childrenList = nodes.stream().filter(c -> c.getParentId().equals(id)).collect(Collectors.toList());
            if (childrenList.size() > 0) {
                childrenList.forEach(c -> {
                    c.setPath(n.getPath() + "/" + c.getId());
                    c.setNamePath(n.getNamePath() + "/" + c.getContent());
                });
            }
        });

        out.println(nodes);
    }
    
    @Test
    void subListTest() {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<String> paths = new ArrayList<>();
        for (int i = 2; i <= integers.size(); i++) {
            paths.add(integers.subList(0, i).stream().map(Objects::toString).collect(Collectors.joining("/")));
        }
        out.println(integers.subList(0, integers.size() - 1).stream().map(Objects::toString).collect(Collectors.joining("/")));
        out.println(paths);
    }
    
    @Test
    void randomTest() {
        Random random = new Random();
        out.println("B" + random.nextInt(4));
//        for (int i = 0; i < 10; i++) {
//            out.println("a" + random.nextInt(i));
//        }
    }

    /**
     This is a guidebook to the many different styles of meditation, the various benefits of each practice, 
     plus free guided audio practices that help you learn how to meditate.

     How do you learn to meditate? In mindfulness meditation, 
     we’re learning how to pay attention to the breath as it goes in and out, 
     and notice when the mind wanders from this task.
     This practice of returning to the breath builds the muscles of attention and mindfulness.

     When we pay attention to our breath, we are learning how to return to, and remain in, 
     the present moment—to anchor ourselves in the here and now on purpose, without judgment.

     In mindfulness practice, we are learning how to return to, and remain in, 
     the present moment—to anchor ourselves in the here and now on purpose, without judgment.

     The idea behind mindfulness seems simple—the practice takes patience. 
     Indeed, renowned meditation teacher Sharon Salzberg recounts that her first experience with 
     meditation showed her how quickly the mind gets caught up in other tasks. “I thought, okay, what will it be,
     like, 800 breaths before my mind starts to wander? And to my absolute amazement, it was one breath, and I’d be gone,” says Salzberg.

     While meditation isn’t a cure-all, it can certainly provide some much-needed space in your life. Sometimes, 
     that’s all we need to make better choices for ourselves, our families, and our communities. 
     And the most important tools you can bring with you to your meditation practice are a little patience, 
     some kindness for yourself, and a comfortable place to sit.

     A Basic Meditation for Beginners
     The first thing to clarify: What we’re doing here is aiming for mindfulness, 
     not some process that magically wipes your mind clear of the countless and endless thoughts 
     that erupt and ping constantly in our brains. We’re just practicing bringing our attention to our 
     breath, and then back to the breath when we notice our attention has wandered.

     Get comfortable and prepare to sit still for a few minutes. After you stop reading this, you’re going to simply focus on your own natural inhaling and exhaling of breath.
     Focus on your breath. Where do you feel your breath most? In your belly? In your nose? Try to keep your attention on your inhale and exhale.
     Follow your breath for two minutes. Take a deep inhale, expanding your belly, and then exhale slowly, elongating the out-breath as your belly contracts.
     Welcome back. What happened? How long was it before your mind wandered away from your breath? Did you notice how busy your mind was even without consciously directing it to think about anything in particular? Did you notice yourself getting caught up in thoughts before you came back to reading this? We often have little narratives running in our minds that we didn’t choose to put there, like: “Why DOES my boss want to meet with me tomorrow?” “I should have gone to the gym yesterday.” “I’ve got to pay some bills” or (the classic) “I don’t have time to sit still, I’ve got stuff to do.”

     We “practice” mindfulness so we can learn how to recognize when our minds are doing their normal everyday acrobatics, and maybe take a pause from that for just a little while so we can choose what we’d like to focus on.

     If you experienced these sorts of distractions (and we all do), you’ve made an important discovery: simply put, that’s the opposite of mindfulness. It’s when we live in our heads, on automatic pilot, letting our thoughts go here and there, exploring, say, the future or the past, and essentially, not being present in the moment. But that’s where most of us live most of the time—and pretty uncomfortably, if we’re being honest, right? But it doesn’t have to be that way.

     We “practice” mindfulness so we can learn how to recognize when our minds are doing their normal everyday acrobatics, and maybe take a pause from that for just a little while so we can choose what we’d like to focus on. In a nutshell, meditation helps us have a much healthier relationship with ourselves (and, by extension, with others).
     */
}
