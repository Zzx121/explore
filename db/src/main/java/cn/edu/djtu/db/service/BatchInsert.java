package cn.edu.djtu.db.service;


import cn.edu.djtu.db.entity.Customer;

import java.util.List;

/**
 * @ClassName BatchInsert
 * @Description: TODO
 * @Author zzx
 * @Date 2020/5/14
 **/
public class BatchInsert implements Runnable{
    private final int startIndex;
    private final int endIndex;
    private final List<Customer> customers;

    public BatchInsert(List<Customer> customers, int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.customers = customers;
    }

    @Override
    public void run() {
        List<Customer> transList = customers.subList(startIndex, endIndex);
    }
}
