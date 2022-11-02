package com.syf.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json工具类
 * @author syf
 */
public abstract class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonUtil() {

    }

    /**
     * 将对象转换为json字符串
     * @param o
     */
    public static String writeJson(Object o) {
        try {
           return  OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
