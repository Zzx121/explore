package cn.edu.djtu.excel.service;

import cn.edu.djtu.excel.entity.Customer;
import cn.edu.djtu.excel.entity.Gender;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @ClassName Reader
 * @Description: excel reader thread
 * @Author zzx
 * @Date 2020/5/14
 **/
public class ExcelReader implements Callable<List<Customer>> {

    @Override
    public List<Customer> call() throws Exception {
        Workbook wb = WorkbookFactory.create(new File("F://tmp/customer.xlsx"));
        List<Customer> customers = new ArrayList<>();
        for (Sheet sheet : wb) {
            for (Row row : sheet) {
                if (row.getRowNum() != 0) {
                    Customer customer = new Customer();
                    customer.setName(row.getCell(0).getStringCellValue());
                    customer.setGender(Gender.getGenderByValue((int) row.getCell(1).getNumericCellValue()));
                    customer.setCellphone(Double.toString(row.getCell(2).getNumericCellValue()));
                    customer.setCompany(row.getCell(3).getStringCellValue());
                    customer.setRemarks(row.getCell(4).getStringCellValue());
                    customer.setBirthday(row.getCell(5).getDateCellValue().toInstant().
                            atZone(ZoneId.systemDefault()).toLocalDate());
                    customer.setGmtCreate(LocalDateTime.now());
                    customer.setIsDeleted(0);
                    customers.add(customer);
                }
            }
        }
        return customers;
    }
}
