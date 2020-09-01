package cn.edu.djtu.excel.common.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * @author zzx
 */
public class PhoneAlreadyUsedException extends AbstractThrowableProblem {
    public PhoneAlreadyUsedException() {
        super(null, ErrorCodeEnum.PHONE_IN_USE.msg(), Status.BAD_REQUEST, "需要重新输入手机号");
    }
}
