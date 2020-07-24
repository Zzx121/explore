package cn.edu.djtu.excel.entity;

import cn.edu.djtu.excel.common.annotation.Excel;
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
    @Excel(name = "姓名", width = 12)
    private String name;
    private Gender gender;
    @Excel(name = "手机号", targetAttr = "mobile")
    @Excel(name = "座机号", targetAttr = "landline")
    private String cellphone;
    private String company;
    private String remarks;
    private LocalDate birthday;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtUpdated;
    private Integer isDeleted;
    
}
