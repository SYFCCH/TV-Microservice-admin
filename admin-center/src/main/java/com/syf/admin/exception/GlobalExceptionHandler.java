package com.syf.admin.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 前后端分离异常处理
 * 错误信息以json格式返回给前端
 * @author syf
 */

@ControllerAdvice
public class GlobalExceptionHandler {
   private static Logger log  = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 运行时异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Map<String,String> exception(Exception e) {
        log.info("运行时异常: {}",e.getMessage());
        Map<String,String> map = new HashMap<>();
        map.put("msg",e.getMessage());
        return map;
    }

}
