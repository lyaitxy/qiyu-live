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
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 滑动窗口计数器
     */
    private static final String SLIDING_WINDOW_LUA_SCRIPT_TEXT = """
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
        SLIDING_WINDOW_SCRIPT.setScriptText(SLIDING_WINDOW_LUA_SCRIPT_TEXT);
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
                String baseKey = applicationName + ":" + userId + ":" + request.getRequestURI();
                String cacheKey = "reqlimit" + requestLimit.strategy().name() + ":" + baseKey;
                // 限制访问数量上限
                int limit = requestLimit.limit();
                // 时间窗口
                int second = requestLimit.second();

                boolean allowed = false;
                // 根据策略进行分发
                switch (requestLimit.strategy()) {
                    case FIXED_WINDOW -> allowed = handlerFixedWindow(cacheKey, limit, second);
                    case SLIDING_WINDOW -> allowed = handlerSlidingWindow(cacheKey, limit, second);
                    case TOKEN_BUCKET -> allowed = handlerTokenBucket(cacheKey, requestLimit.capacity(), requestLimit.limit(), requestLimit.second());
                    default -> allowed = true;
                }

                if(allowed) return true;

                // 超过限流数量上限
                // 直接抛出全局异常， 让异常捕获器处理
                LOGGER.error("[RequestLimitInterceptor] userId is {}, req too much", userId);
                throw new QiyuErrorException(-10001, requestLimit.msg());
            }
        }
        return true;
    }

    // 固定窗口
    private boolean handlerFixedWindow(String cacheKey, int limit, int second) {
        Integer reqTime = (Integer) Optional.ofNullable(redisTemplate.opsForValue().get(cacheKey)).orElse(0);
        if (reqTime == 0) {
            redisTemplate.opsForValue().set(cacheKey, 1, second, TimeUnit.SECONDS);
            return true;
        } else if (reqTime < limit) {
            redisTemplate.opsForValue().increment(cacheKey, 1);
            return true;
        }
        // 超过限制
        return false;
    }

    // 滑动窗口
    private boolean handlerSlidingWindow(String cacheKey, int limit, int second) {
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
        return false;
    }

    // 令牌桶逻辑
    private static final String TOKEN_BUCKET_LUA_SCRIPT_TEXT = """
            --[[
              令牌桶限流算法 Lua 脚本
                        
              KEYS[1]: 限流的 Key (例如: limit:tokenbucket:userId:uri)
                        
              ARGV[1]: capacity (桶的容量)
              ARGV[2]: rate (令牌生成速率, 每秒 N 个)
              ARGV[3]: now (当前时间戳, 毫秒)
              ARGV[4]: requested (本次请求需要消耗的令牌数, 通常为 1)
              ARGV[5]: expire_sec (Key 的过期时间, 秒, 用于防止冷数据)
                        
              返回值:
                1: 允许
                0: 拒绝
            --]]
                        
            -- 1. 获取参数
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local rate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])
            local expire_sec = tonumber(ARGV[5])
                        
            -- 2. 获取桶的当前状态 (上次剩余令牌, 上次刷新时间)
            local data = redis.call('HMGET', key, 'last_tokens', 'last_timestamp')
            local last_tokens = tonumber(data[1])
            local last_timestamp = tonumber(data[2])
                        
            -- 3. 初始化 (如果是第一次访问)
            if last_tokens == nil then
                last_tokens = capacity
            end
            if last_timestamp == nil then
                last_timestamp = now
            end
                        
            -- 4. 计算新生成的令牌 (核心)
            -- 计算时间差 (毫秒)
            local elapsed_ms = math.max(0, now - last_timestamp)
                        
            -- 计算应生成的新令牌数 (允许浮点数)
            local new_tokens = (elapsed_ms / 1000) * rate
                        
            -- 5. 刷新桶
            -- (上次剩余的 + 新生成的)，不能超过桶的容量
            local current_tokens = math.min(capacity, last_tokens + new_tokens)
            local allowed = 0
            local tokens_to_save = current_tokens
                        
            -- 6. 尝试消费
            if current_tokens >= requested then
                -- 令牌足够
                allowed = 1
                tokens_to_save = current_tokens - requested
            end
                        
            -- 7. 无论是否成功, 都更新桶的状态
            -- 为什么失败也要更新?
            -- 因为即使本次请求失败了, 新生成的令牌也确实“存入”桶里了。
            -- 必须更新时间戳, 否则下次请求会重复计算这段时间的令牌。
            redis.call('HMSET', key, 'last_tokens', tokens_to_save, 'last_timestamp', now)
                        
            -- 8. 设置/刷新过期时间
            redis.call('EXPIRE', key, expire_sec)
                        
            -- 9. 返回结果
            return allowed
            """;
    private static final DefaultRedisScript<Long> TOKEN_BUCKET_SCRIPT = new DefaultRedisScript<>();

    static {
        TOKEN_BUCKET_SCRIPT.setScriptText(TOKEN_BUCKET_LUA_SCRIPT_TEXT);
        TOKEN_BUCKET_SCRIPT.setResultType(Long.class);
    }
    /**
     * 数据结构： 使用 Redis HASH。
     * Key: limit:tokenbucket:userId:uri
     * Field 1: tokens (当前桶内剩余的令牌数)
     * Field 2: timestamp (上次刷新令牌的时间戳)
     * @param cacheKey
     * @param capacity
     * @param second
     * @return
     */
    private boolean handlerTokenBucket(String cacheKey, int capacity, int limit, int second) {
        // 窗口，处理除0异常，默认放行
        if(second <= 0) return true;
        // 计算令牌生产速率
        int rate = limit / second;
        // 获取当前时间戳
        long now = System.currentTimeMillis();
        long requested = 1; // 每次请求消耗一个
        // 过期时间（比窗口稍长，防止冷数据）
        long expire_sec = second + 10;

        // 准备参数
        List<String> keys = Collections.singletonList(cacheKey);
        String capacityStr = String.valueOf(capacity);
        String rateStr = String.valueOf(rate);
        String nowStr = String.valueOf(now);
        String requestedStr = String.valueOf(requested);
        String expireStr = String.valueOf(expire_sec);

        // 执行
        Long result = stringRedisTemplate.execute(
                TOKEN_BUCKET_SCRIPT,
                keys,
                capacityStr,
                rateStr,
                nowStr,
                requestedStr,
                expireStr
        );

        return result != null && result == 1;
    }
}
