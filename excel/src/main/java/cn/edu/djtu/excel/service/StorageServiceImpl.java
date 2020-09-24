package cn.edu.djtu.excel.service;

import cn.edu.djtu.excel.common.exception.StorageException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * @author zzx
 * @date 2020/9/19
 */
public class StorageServiceImpl implements StorageService {
    private final Path rootLocation;

    public StorageServiceImpl(Path rootLocation) {
        this.rootLocation = rootLocation;
    }

    @Override
    public String store(MultipartFile file, boolean encodeFilename) {
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (file.isEmpty()) {
            throw new StorageException("无法保存空文件"+ filename);
        }
        try (InputStream inputStream = file.getInputStream()) {
            if (encodeFilename) {
                filename = encodeFilename(filename);
            }
            if (Files.notExists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new StorageException();
        }
        
        return filename;
    }

    /**
     * 重命名文件
     * @param filename 原文件名
     * @return 重命名之后的文件名
     */
    private String encodeFilename(String filename) {
        String extension = com.google.common.io.Files.getFileExtension(filename);
        String nameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(filename);
        return nameWithoutExtension + "_" + UUID.randomUUID().toString() + extension;
    }
}
