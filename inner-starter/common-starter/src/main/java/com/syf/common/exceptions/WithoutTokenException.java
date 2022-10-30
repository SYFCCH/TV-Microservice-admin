package com.syf.common.exceptions;

/**
 * 没有token
 * @author syf
 */
public class WithoutTokenException extends RuntimeException{
    public WithoutTokenException(String message) {
        super(message);
    }
}
