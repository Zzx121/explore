package cn.edu.djtu.excel.client;

import cn.edu.djtu.excel.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RestTemplateClient
 * @Description: TODO
 * @Author zzx
 * @Date 2020/4/16
 **/
@Component
public class RestTemplateClient {
    @Autowired
    private RestTemplate restTemplate;

 

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    public List<Customer> listAllCustomers() {
        ResponseEntity<List<Customer>> restExchange = restTemplate.exchange("http://db-server:8080/customerList",
                 HttpMethod.GET, null, new ParameterizedTypeReference<List<Customer>>() {});
        return restExchange.getBody();
    }
    
}
