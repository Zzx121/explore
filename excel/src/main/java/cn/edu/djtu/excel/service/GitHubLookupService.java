package cn.edu.djtu.excel.service;

import cn.edu.djtu.excel.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author zzx
 * @date 2020/12/7
 */
@Service
public class GitHubLookupService {
    private static final Logger logger = LoggerFactory.getLogger(GitHubLookupService.class);
    private final RestTemplate restTemplate;

    public GitHubLookupService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Async
    public CompletableFuture<User> findUser(String user) throws InterruptedException {
        logger.info("Looking up" + user);
        String url = String.format("https://api.github.com/users/%s", user);
        User result = restTemplate.getForObject(url, User.class);
        Thread.sleep(1000L);
        
        return CompletableFuture.completedFuture(result);
    }
}
