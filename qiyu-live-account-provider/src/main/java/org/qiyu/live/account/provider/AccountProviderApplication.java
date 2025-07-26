package org.qiyu.live.account.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.account.provider.service.IAccountTokenService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class AccountProviderApplication implements CommandLineRunner {

    @Resource
    private IAccountTokenService accountTokenService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(AccountProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        Long userId = 1092813L;
        String token = accountTokenService.createAndSaveLoginToken(userId);
        System.out.println("token = " + token);
        Long userIdByToken = accountTokenService.getUserIdByToken(token);
        System.out.println("matchUserId is " + userIdByToken);

    }
}
