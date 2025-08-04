package org.qiyu.live.user.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.user.interfaces.dto.UserLoginDTO;
import org.qiyu.live.user.provider.service.IUserPhoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户中台服务提供者
 */
@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class UserProviderApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProviderApplication.class);

    @Resource
    private IUserPhoneService userPhoneService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(UserProviderApplication.class);
        // 不涉及web场景
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        String phone = "18789829049";
        UserLoginDTO userLoginDTO = userPhoneService.login(phone);
        System.out.println(userLoginDTO);
        System.out.println(userPhoneService.queryByUserId(userLoginDTO.getUserId()));
        System.out.println(userPhoneService.queryByUserId(userLoginDTO.getUserId()));
        System.out.println(userPhoneService.queryByPhone(phone));
        System.out.println(userPhoneService.queryByPhone(phone));
    }
}
