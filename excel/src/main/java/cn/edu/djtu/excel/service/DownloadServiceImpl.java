package cn.edu.djtu.excel.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author zzx
 * @date 2020/10/13
 */
public class DownloadServiceImpl implements DownloadService {
    @Override
    public ResponseEntity<Resource> downloadFromOuterUrl(String url, String filePrefix) {
        try(BufferedInputStream bis = new BufferedInputStream(new URL(url).openStream())) {
            ByteArrayResource resource = new ByteArrayResource(bis.readAllBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+ generateEncodedFilename(url, filePrefix));
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("msg", "文件获取失败");
            map.add("code", "0");
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    private String generateEncodedFilename(String url, String filePrefix) {
        String suffix = url.substring(url.lastIndexOf("."));
        return URLEncoder.encode(filePrefix + "_" + UUID.randomUUID().toString() + suffix, StandardCharsets.UTF_8);
    }
}
