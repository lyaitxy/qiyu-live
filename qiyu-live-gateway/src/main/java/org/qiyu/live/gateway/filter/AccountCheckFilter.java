package org.qiyu.live.gateway.filter;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.account.interfaces.IAccountTokenRPC;
import org.qiyu.live.common.interfaces.enums.GatewayHeaderEnum;
import org.qiyu.live.gateway.properties.GatewayApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static io.netty.handler.codec.http.cookie.CookieHeaderNames.MAX_AGE;
import static org.springframework.web.cors.CorsConfiguration.ALL;

@Component
public class AccountCheckFilter implements GlobalFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountCheckFilter.class);

    @DubboReference
    private IAccountTokenRPC accountTokenRPC;
    @Resource
    private GatewayApplicationProperties gatewayApplicationProperties;

    /**
     *
     * @param exchange 包含整个HTTP请求和响应的上下文
     * @param chain 过滤器链对象，通过chain.filter(exchange)放行
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求url.
        ServerHttpRequest request = exchange.getRequest();
        String reqUrl = request.getURI().getPath();

        // 允许跨域请求（设置了这里得删除其它两处跨域处理）
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        // 这里我们不设置域名，就设置为localhost
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5500");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, ALL);
        headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);

        if(StringUtils.isEmpty(reqUrl)) {
            return Mono.empty();
        }
        List<String> notCheckUrlList = gatewayApplicationProperties.getNotCheckUrlList();
        for(String notCheckUrl : notCheckUrlList) {
            if(reqUrl.startsWith(notCheckUrl)) {
                LOGGER.info("请求没有进行token校验，直接传递给业务下游");
                return chain.filter(exchange);
            }
        }

        // 如果不存在
        List<HttpCookie> httpCookieList = request.getCookies().get("qytk");
        if(CollectionUtils.isEmpty(httpCookieList)) {
            LOGGER.info("请求没有检索到qytk的cookie，被拦截");
            return Mono.empty();
        }
        String qiyuTokenCookieValue = httpCookieList.get(0).getValue();
        if (StringUtils.isEmpty(qiyuTokenCookieValue) || StringUtils.isEmpty(qiyuTokenCookieValue.trim())) {
            LOGGER.info("检索到qytk的cookie是空，被拦截");
            return Mono.empty();
        }
        // token获取到后，调用rpc判断token是否合法，如果合法把token换取到的userId传递给下游
        Long userId = accountTokenRPC.getUserIdByToken(qiyuTokenCookieValue);
        if (userId == null){
            LOGGER.error("请求的token失效了，被拦截");
            return Mono.empty();
        }
        // gateway --> (header) --> springboot
        // 将用户id放到请求头中
        ServerHttpRequest.Builder builder = request.mutate();
        builder.header(GatewayHeaderEnum.USER_LOGIN_ID.getName(), String.valueOf(userId));
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
