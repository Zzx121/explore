package cn.edu.djtu.excel.common.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = ApplicationProperty.STORAGE_PREFIX)
@Component
@Getter
@Setter
public class ApplicationProperty {
    public static final String STORAGE_PREFIX = "file.storage";
    private String basePath;
    private String excelPath;
}
