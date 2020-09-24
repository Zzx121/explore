package cn.edu.djtu.excel.util.poi;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * @author zzx
 */
public class ExcelImportFileCheckException extends AbstractThrowableProblem {
    public ExcelImportFileCheckException() {
        super(null, "导入文件验证失败", Status.BAD_REQUEST, "可能是表格表头已被改变");
    }
}
