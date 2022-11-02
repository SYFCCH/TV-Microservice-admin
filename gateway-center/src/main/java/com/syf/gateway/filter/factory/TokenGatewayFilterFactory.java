package com.syf.gateway.filter.factory;


import com.syf.common.constants.RedisPrefix;
import com.syf.common.exceptions.IllegalTokenException;
import com.syf.common.exceptions.WithoutTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import java.util.List;

/**
 * 自定义Token的网关局部过滤器工厂
 * @author  syf
 */
@Component
public class TokenGatewayFilterFactory extends AbstractGatewayFilterFactory<TokenGatewayFilterFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(TokenGatewayFilterFactory.class);

    private  static final String REQUIRED_TOKEN = "requiredToken";
    private RedisTemplate redisTemplate;

    @Autowired
    public TokenGatewayFilterFactory(RedisTemplate redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    /**
     * 带着token访问网关，如果token验证通过才放行
     * @param config
     * @return
     */
    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            //exchange里面有request 和 response的信息
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                log.info("进入filter...");
                log.info("config required token: {}", config.requiredToken);
                if(config.requiredToken) {
                    //网关拦截获取token信息
                    List<String> tokens = exchange.getRequest().getQueryParams().get("token");
                    if (tokens == null) {
                        throw new WithoutTokenException("没有令牌!");
                    }
                    //我们业务只有一个token
                    String token = tokens.get(0);
                    log.info("token:{}", token);
                    //去redis中找token
                    if (!redisTemplate.hasKey(RedisPrefix.TOKEN_KEY + token)) {
                        throw new IllegalTokenException("令牌不合法!");
                    }
                }
                    return chain.filter(exchange);
            }
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(REQUIRED_TOKEN);
    }

    public  static class Config{
        private boolean requiredToken;

        public boolean isRequiredToken() {
            return requiredToken;
        }

        public void setRequiredToken(boolean requiredToken) {
            this.requiredToken = requiredToken;
        }
    }
}
