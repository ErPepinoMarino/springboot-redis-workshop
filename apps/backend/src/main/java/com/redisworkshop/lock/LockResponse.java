package com.redisworkshop.lock;

public record LockResponse(boolean ok, String holder, long counter) {
}