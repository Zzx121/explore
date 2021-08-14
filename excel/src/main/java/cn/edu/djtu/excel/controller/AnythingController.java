package cn.edu.djtu.excel.controller;

import cn.edu.djtu.excel.common.exception.PhoneAlreadyUsedException;
import cn.edu.djtu.excel.entity.Customer;
import cn.edu.djtu.excel.entity.Gender;
import cn.edu.djtu.excel.service.ExcelService;
import cn.edu.djtu.excel.util.basic.RequestUtil;
import cn.edu.djtu.excel.util.basic.StringUtil;
import cn.edu.djtu.excel.util.poi.ExcelUtil;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.AbstractThrowableProblem;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author zzx
 * @date 2020/4/16
 **/
@Log
@RestController
@RequestMapping("/anything")
public class AnythingController {
    
    @GetMapping("/tokenFilter")
    public void tokenFilter(HttpServletRequest request) {
        log.info("json body third time: " + RequestUtil.getRequestParamValue(request, "token"));
        throw new PhoneAlreadyUsedException();
    }
    
}
