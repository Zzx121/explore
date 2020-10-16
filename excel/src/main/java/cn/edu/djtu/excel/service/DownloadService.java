package cn.edu.djtu.excel.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

/**
 * @author zzx
 * @date 2020/10/13
 */
public interface DownloadService {
    /**
     * 从外部链接下载文件 
     * @param url url
     * @param filePrefix 前缀
     * @return 文件流
     */
    ResponseEntity<Resource> downloadFromOuterUrl(String url, String filePrefix);
}
