package cn.edu.djtu.excel.service;


import cn.edu.djtu.excel.client.OpenFeignClient;
import cn.edu.djtu.excel.client.RestTemplateClient;
import cn.edu.djtu.excel.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName ExportFromDB
 * @Description: 从数据库导出数据到excel
 * @Author zzx
 * @Date 2020/2/29
 **/
@Service
public class ExcelService {
    @Autowired
    private RestTemplateClient restTemplateClient;
    @Autowired
    private OpenFeignClient openFeignClient;
   public List<Customer> getByRestTemplate() {
       return restTemplateClient.listAllCustomers();
   }
   
   public List<Customer> getByOpenFeign() {
       return openFeignClient.listAllCustomers();
   }
   
   public void addCustomer(Customer customer) {
       openFeignClient.addCustomer(customer);
   } 
   
   public void batchInsertCustomers(List<Customer> customers) {
       openFeignClient.batchInsertCustomers(customers);
   }
}
