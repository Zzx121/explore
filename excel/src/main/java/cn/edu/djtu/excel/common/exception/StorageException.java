package cn.edu.djtu.excel.common.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;

import java.net.URI;

/**
 * 文件保存异常
 * @author zzx
 * @date 2020/9/19
 */
public class StorageException extends AbstractThrowableProblem {
    public StorageException(URI type, String title, StatusType status) {
        super(type, title, status);
    }

    public StorageException() {
        this("文件保存失败");
    }

    public StorageException(String title) {
        this(null, title, Status.INTERNAL_SERVER_ERROR);
    }
}
