package cn.edu.djtu.db.dao;

import cn.edu.djtu.db.entity.Customer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerMapper {
    int insertCustomer(Customer customer);
    void batchInsertCustomers(@Param("customers") List<Customer> customers);
    List<Customer> listAllCustomer();
}
