package cn.edu.djtu.excel.controller;

import cn.edu.djtu.excel.common.exception.PhoneAlreadyUsedException;
import cn.edu.djtu.excel.entity.Customer;
import cn.edu.djtu.excel.entity.Gender;
import cn.edu.djtu.excel.service.DownloadService;
import cn.edu.djtu.excel.service.ExcelService;
import cn.edu.djtu.excel.util.basic.StringUtil;
import cn.edu.djtu.excel.util.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.AbstractThrowableProblem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @ClassName ExcelController
 * @Description: TODO
 * @Author zzx
 * @Date 2020/4/16
 **/
@RestController
public class ExcelController {
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private DownloadService downloadService;
    @GetMapping("/rest")
    public List<Customer> getByRestTemplate() {
        return excelService.getByRestTemplate();
    }
    
    @GetMapping("/feign")
    public List<Customer> getByFeign() {
        return excelService.getByOpenFeign();
    }
    
    @PostMapping("/customer")
    public void addCustomer(@RequestBody Customer customer) {
        excelService.addCustomer(customer);
    }
    
    @PostMapping("/customers")
    public void batchInsertCustomers(@RequestBody List<Customer> customers) {
        excelService.batchInsertCustomers(customers);
    }

    @ExceptionHandler(AbstractThrowableProblem.class)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<String> excelExceptionHandler(AbstractThrowableProblem exception) {
        String title = exception.getTitle();
        String detail = exception.getDetail();
        String msg = title;
        if (StringUtil.isNotEmpty(detail)) {
            msg += detail;
        }
        return ResponseEntity.ok(Objects.requireNonNull(msg));
    }

    @PostMapping("/export")
    @ResponseBody
    public String export() {
        Map<String, Map<String, Object>> expKeyMap = new HashMap<>(4);
        Map<String, Object> sexMap = new HashMap<>(3);
        sexMap.put("男", Gender.MALE);
        sexMap.put("女", Gender.FEMALE);
        sexMap.put("未知", Gender.SECRET);
        expKeyMap.put("gender", sexMap);
        ExcelUtil<Customer> util = new ExcelUtil<>(Customer.class, expKeyMap);
        return util.exportExcel(prepareList(20), "用户数据");
    }
    
    @GetMapping("/throwsPhoneInUse")
    public void throwsTest() {
        throw new PhoneAlreadyUsedException();
    }
    
    private List<Customer> prepareList(int count) {
        List<Customer> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Customer customer = new Customer();
            customer.setGender(i % 2 == 0 ? Gender.FEMALE : Gender.MALE);
            customer.setName("name + " + i);
            customer.setBirthday(LocalDate.now());
            customer.setCellphone("1387865789" + i);
            customer.setGmtCreate(LocalDateTime.now());
            list.add(customer);
        }
        
        return list;
    }
    
    @GetMapping("/download")
    public ResponseEntity<Resource> download(String url) {
        return downloadService.downloadFromOuterUrl(url, "test");
    }
    
}
