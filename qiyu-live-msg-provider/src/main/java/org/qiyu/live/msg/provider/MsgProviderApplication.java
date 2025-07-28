package org.qiyu.live.msg.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.msg.dto.MsgCheckDTO;
import org.qiyu.live.msg.enums.MsgSendResultEnum;
import org.qiyu.live.msg.provider.service.ISmsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.Scanner;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class MsgProviderApplication implements CommandLineRunner {

    @Resource
    private ISmsService smsService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication((MsgProviderApplication.class));
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }


    @Override
    public void run(String... args) throws Exception {
//        String phoneStr = "17665753022";
//        MsgSendResultEnum msgSendResultEnum = smsService.sendLoginCode(phoneStr);
//        System.out.println(msgSendResultEnum);
//        while(true) {
//            System.out.println("输出验证码：");
//            Scanner scanner = new Scanner(System.in);
//            int code = scanner.nextInt();
//            MsgCheckDTO checkStatus = smsService.checkLoginCode(phoneStr, code);
//            System.out.println(checkStatus);
//        }
    }
}
