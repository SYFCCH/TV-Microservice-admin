package com.syf.common.exceptions;

/**
 * 非法的token请求
 * @author syf
 */
public class IllegalTokenException extends RuntimeException {

    public IllegalTokenException(String message) {super(message);}
}
