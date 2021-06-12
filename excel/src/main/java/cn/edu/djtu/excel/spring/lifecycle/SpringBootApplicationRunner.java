package cn.edu.djtu.excel.spring.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author zzx
 * @date 2021/4/20
 */
@Component
public class SpringBootApplicationRunner implements ApplicationRunner {
    private static Logger LOG = LoggerFactory
            .getLogger(SpringBootApplicationRunner.class);
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOG.info("EXECUTING : Run method of Application Runner");
        List<String> nonOptionArgs = args.getNonOptionArgs();
        String[] sourceArgs = args.getSourceArgs();
        Set<String> optionNames = args.getOptionNames();
        nonOptionArgs.forEach(nonOption -> LOG.info("## Non Option arg : " + nonOption));
        optionNames.forEach(optionName -> LOG.info("## Option Name : " + optionName));
        Arrays.stream(sourceArgs).forEach(sourceArg -> LOG.info("## Source arg : " + sourceArg));
        LOG.info("## Option Arg1 value : " + args.getOptionValues("optionArg1"));
        LOG.info("## Option Arg2 value : " + args.getOptionValues("optionArg2"));
    }

    public static void main(String[] args) {
        LOG.info("STARTING : Spring boot application starting");
        SpringApplication.run(SpringBootApplicationRunner.class, args);
        LOG.info("STOPPED  : Spring boot application stopped");
    }
}
