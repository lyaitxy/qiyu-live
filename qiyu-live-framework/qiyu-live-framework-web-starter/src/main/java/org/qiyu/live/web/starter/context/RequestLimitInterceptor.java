package org.qiyu.live.web.starter.context;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.qiyu.live.web.starter.error.QiyuErrorException;
import org.qiyu.live.web.starter.limit.RequestLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 对于重复请求，要有专门的拦截器去处理，进行相同用户下的限流
 */
public class RequestLimitInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLimitInterceptor.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 滑动窗口计数器
     */
    private static final String LUA_SCRIPT_TEXT = """
    local key = KEYS[1]
    local window_start = ARGV[1]
    local current_time = ARGV[2]
    local member = ARGV[3]
    local limit = tonumber(ARGV[4])
    local expire_sec = tonumber(ARGV[5])

    -- 1. 移除窗口之外的旧请求
    redis.call('ZREMRANGEBYSCORE', key, 0, window_start)

    -- 2. 添加当前请求
    redis.call('ZADD', key, current_time, member)

    -- 3. 获取窗口内的总请求数
    local count = redis.call('ZCARD', key)

    -- 4. 设置一个安全过期时间，防止冷数据
    redis.call('EXPIRE', key, expire_sec)

    -- 5. 判断是否超过限制
    if count > limit then
        return 0 -- 0 表示失败 (超过限制)
    end
    return 1 -- 1 表示成功 (允许访问)
    """;

    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>();
    static {
        SLIDING_WINDOW_SCRIPT.setScriptText(LUA_SCRIPT_TEXT);
        SLIDING_WINDOW_SCRIPT.setResultType(Long.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 查看有无 @RequestLimit 注解标识该方法
            boolean hasLimit = handlerMethod.getMethod().isAnnotationPresent(RequestLimit.class);
            if (hasLimit) {
                RequestLimit requestLimit = handlerMethod.getMethod().getAnnotation(RequestLimit.class);
                Long userId = QiyuRequestContext.getUserId();
                // 没有userId标识是网关通过的白名单请求，放行
                if (userId == null) {
                    return true;
                }
                // 缓存key是应用名 + 用户名 + 请求路径
                String cacheKey = applicationName + ":" + userId + ":" + request.getRequestURI();
                // 限制访问数量上限
                int limit = requestLimit.limit();
                // 时间窗口
                int second = requestLimit.second();

                // 滑动窗口限流逻辑
                long now = System.currentTimeMillis();
                long windowStart = now - (second * 1000L); // 窗口开始时间
                String member = UUID.randomUUID().toString(); // 当前请求的唯一ID
                // 准备lua脚本参数
                List<String> keys = Collections.singletonList(cacheKey);
                // 将所有参数转为字符串
                String windowStartStr = String.valueOf(windowStart);
                String nowStr = String.valueOf(now);
                String limitStr = String.valueOf(limit);
                // 安全过期时间（比窗口稍长）
                String expireSecStr = String.valueOf(second + 2);

                // 执行 Lua 脚本
                // 推荐使用 StringRedisTemplate
                Long result = stringRedisTemplate.execute(
                        SLIDING_WINDOW_SCRIPT,
                        keys,
                        windowStartStr,
                        nowStr,
                        member,
                        limitStr,
                        expireSecStr
                );
                // Lua 脚本返回 1 表示允许，0 表示拒绝
                if (result != null && result == 1) {
                    // 允许访问
                    return true;
                }

                // 固定窗口逻辑
//                Integer reqTime = (Integer) Optional.ofNullable(redisTemplate.opsForValue().get(cacheKey)).orElse(0);
//                if (reqTime == 0) {
//                    redisTemplate.opsForValue().set(cacheKey, 1, second, TimeUnit.SECONDS);
//                    return true;
//                } else if (reqTime < limit) {
//                    redisTemplate.opsForValue().increment(cacheKey, 1);
//                    return true;
//                }
                // 超过限流数量上限
                // 直接抛出全局异常， 让异常捕获器处理
                LOGGER.error("[RequestLimitInterceptor] userId is {}, req too much", userId);
                throw new QiyuErrorException(-10001, requestLimit.msg());
            }
        }
        return true;
    }
}
