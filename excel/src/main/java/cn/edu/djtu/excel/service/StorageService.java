package cn.edu.djtu.excel.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author zzx
 * @date 2020/9/19
 */
public interface StorageService {
    /**
     * 保存文件
     * @param file file
     * @param encodeFilename 是否重命名文件
     * @return 保存的文件名
     */
    String store(MultipartFile file, boolean encodeFilename);
}
