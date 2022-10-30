package com.syf.gateway.filter.factory;


import com.syf.common.constants.RedisPrefix;
import com.syf.common.exceptions.ConfigException;
import com.syf.common.exceptions.IllegalTokenException;
import com.syf.common.exceptions.WithoutTokenException;
import com.syf.redis.config.RedisAutoConfig;
import com.syf.redis.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
    ApplicationContext context = new AnnotationConfigApplicationContext(RedisAutoConfig.class);
    RedisUtils redisUtils;

    @Autowired
    public TokenGatewayFilterFactory() {
        super(Config.class);
        redisUtils = (RedisUtils) context.getBean("redisUtils");
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
                    if (redisUtils.hasKey(RedisPrefix.TOKEN_KEY + token)) {
                        throw new IllegalTokenException("令牌不合法!");
                    }
                }else{
                    throw new ConfigException("配置没开启");
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
