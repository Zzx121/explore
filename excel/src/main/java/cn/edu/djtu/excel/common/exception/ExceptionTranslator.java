package cn.edu.djtu.excel.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/**
 * 全局异常处理
 * @author zzx
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling {
    @ExceptionHandler
    public ResponseEntity<Problem> handlePhoneAlreadyUsedException(PhoneAlreadyUsedException ex, NativeWebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();
        return create(ex, request);
    }
}
