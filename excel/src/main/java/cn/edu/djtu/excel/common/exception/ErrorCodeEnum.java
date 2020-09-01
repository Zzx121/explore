package cn.edu.djtu.excel.common.exception;

import org.springframework.http.HttpStatus;

/**
 * @author zzx
 */

public enum ErrorCodeEnum {
    /**
     * 号码已被使用
     */
    PHONE_IN_USE(HttpStatus.BAD_REQUEST.value(), "号码已被使用")
    ;
    private final int status;
    private final String msg;

    ErrorCodeEnum(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
    
    public int status() {
        return this.status;
    }
    
    public String msg() {
        return this.msg;
    }

    /**
     * 转换为 ErrorCode
     */
    public ErrorCode convert() {
        return ErrorCode.builder().msg(msg()).name(name()).status(status()).build();
    }

    /**
     * 自定义返回消息
     * @param msg 要自定义的消息内容
     */
    public ErrorCode overrideMsg(String msg) {
        return ErrorCode.builder().msg(msg).name(name()).status(status()).build();
    }
    
    
}
