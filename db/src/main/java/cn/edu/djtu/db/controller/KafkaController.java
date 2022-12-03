package cn.edu.djtu.db.controller;

import cn.edu.djtu.db.service.KafkaTransactionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName KafkaController
 * @Description: TODO
 * @Author zzx
 * @Date 2022/9/22
 **/
@RestController("/kafka")
public class KafkaController {
    private final KafkaTransactionService kafkaTransactionService;

    public KafkaController(KafkaTransactionService kafkaTransactionService) {
        this.kafkaTransactionService = kafkaTransactionService;
    }

    @PostMapping("/produce")
    public void sendMessage(String payload) {
        kafkaTransactionService.sendMessage(payload);
    }
}
