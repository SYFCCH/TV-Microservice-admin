package com.syf.gateway.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syf.common.exceptions.ConfigException;
import com.syf.common.exceptions.IllegalTokenException;
import com.syf.common.exceptions.WithoutTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关异常处理类
 * @author syf
 */

@Order(-1)
@Configuration
public class GlobalExceptionConfiguration implements ErrorWebExceptionHandler {
    public static final Logger log = LoggerFactory.getLogger(GlobalExceptionConfiguration.class);


    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        Map<String,String> result = new HashMap<>();

        ServerHttpResponse response = serverWebExchange.getResponse();
        if(response.isCommitted()) {
            return Mono.error(throwable);
        }
        //设置响应格式
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        //设置响应状态码
        if(throwable instanceof IllegalTokenException) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
        }else if(throwable instanceof ConfigException) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        }else if(throwable instanceof WithoutTokenException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
        }else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response.writeWith(Mono.fromSupplier(()->{
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            result.put("msg", throwable.getMessage());
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return dataBufferFactory.wrap(objectMapper.writeValueAsBytes(result));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }));

    }
}
