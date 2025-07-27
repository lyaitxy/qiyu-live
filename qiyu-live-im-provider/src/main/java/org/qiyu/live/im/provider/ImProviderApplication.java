package org.qiyu.live.im.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.provider.service.ImTokenService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class ImProviderApplication implements CommandLineRunner {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SpringApplication springApplication = new SpringApplication(ImProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
        countDownLatch.await();
    }

    @Resource
    private ImTokenService imTokenService;

    @Override
    public void run(String... args) throws Exception {
        Long userId = 11113L;
        String token = imTokenService.createImLoginToken(userId, AppIdEnum.QIYU_LIVE_BIZ.getCode());
        System.out.println("token is " + token);
        Long userIdByToken = imTokenService.getUserIdByToken(token);
        System.out.println("userIdResult is " + userIdByToken);
    }
}
