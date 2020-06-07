package cn.edu.djtu.excel.controller;

import cn.edu.djtu.excel.entity.Customer;
import cn.edu.djtu.excel.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
