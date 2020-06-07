package cn.edu.djtu.excel.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @ClassName Customer
 * @Description: TODO
 * @Author zzx
 * @Date 2020/3/3
 **/
@Getter
@Setter
public class Customer implements Serializable {
    private Integer id;
    private String name;
    private Gender gender;
    private String cellphone;
    private String company;
    private String remarks;
    private LocalDate birthday;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtUpdated;
    private Integer isDeleted;
    
}
