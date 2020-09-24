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
    @Excel(name = "生日", dateFormat = "yyyy-MM-dd", type = Excel.Type.EXPORT)
    private LocalDate birthday;
    @Excel(name = "性别", readConverterExp = "MALE=男,FEMALE=女,SECRET=未知", type = Excel.Type.EXPORT)
//    @Excel(name = "性别", readConverterKey = "gender")
    private Gender gender;
    @Excel(name = "手机号")
    private String cellphone;
    @Excel(name = "公司", type = Excel.Type.IMPORT)
    private String company;
    @Excel(name = "备注")
    private String remarks;
    @Excel(name = "创建日期", dateFormat = "yyyy-MM-dd HH:mm:ss", width = 20, type = Excel.Type.EXPORT)
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtUpdated;
    private Integer isDeleted;
    
}
