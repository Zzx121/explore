package cn.edu.djtu.excel.client;

import cn.edu.djtu.excel.entity.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName OpenFeignClient
 * @Description: TODO
 * @Author zzx
 * @Date 2020/4/16
 **/
@FeignClient(name="http://db-server:8080", url = "http://db-server:8080")
public interface OpenFeignClient {
    @GetMapping("/customerList")
    List<Customer> listAllCustomers();
    @PostMapping("/customer")
    void addCustomer(@RequestBody Customer customer);
    @PostMapping("/customers")
    void batchInsertCustomers(@RequestBody List<Customer> customers);
}
