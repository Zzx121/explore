package cn.edu.djtu.excel.common.exception;

import lombok.*;

/**
 * @author zzx
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = false)
public class ErrorCode {
    /**
     * ErrorCodeEnum.name()
     */
    private String name;
    /**
     * Http 状态码
     */
    private int status;
    /**
     * 错误消息
     */
    private String msg;
}
