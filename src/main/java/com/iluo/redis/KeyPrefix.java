package com.iluo.redis;

public interface KeyPrefix {

    public int expireSeconds() ;

    public String getPrefix() ;

}
