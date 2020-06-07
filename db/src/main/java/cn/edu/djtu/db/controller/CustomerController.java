package cn.edu.djtu.db.controller;

import cn.edu.djtu.db.entity.Customer;
import cn.edu.djtu.db.entity.Gender;
import cn.edu.djtu.db.service.CustomerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @ClassName CustomerController
 * @Description: TODO
 * @Author zzx
 * @Date 2020/3/4
 **/
@RestController
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    
    @PostMapping("/customer")
    public void insertCustomer(@RequestBody Customer customer) {
        customer.setGmtCreate(LocalDateTime.now());
        customer.setIsDeleted(0);
        customerService.insertCustomer(customer);
    }
    
    @PostMapping("/customers")
    public void batchInsertCustomers(@RequestBody List<Customer> customers) {
        try {
            List<Customer> customerList = produceCustomers(100000);
            long start = System.currentTimeMillis();
            customerService.batchInsertCustomers(customerList);
            long stop = System.currentTimeMillis();
            System.out.println(stop - start);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @GetMapping("/customerList")
    public List<Customer> listAllCustomer() {
        return customerService.listAllCustomer();
    }
    
    @PostMapping("/customersRedis")
    public void batchInsertToRedis(@RequestBody List<Customer> customers) {
        List<Customer> customerList = produceCustomers(100000);
        customerService.saveInRedis(customerList, Customer.class);
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
    
}
