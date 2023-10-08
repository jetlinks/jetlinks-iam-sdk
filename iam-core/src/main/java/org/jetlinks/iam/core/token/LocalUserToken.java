package org.jetlinks.iam.core.token;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
@Getter
@Setter
public class LocalUserToken implements UserToken {

    private static final long serialVersionUID = 1L;

    private String userId;

    private String token;

    private String type = "default";

    private volatile TokenState state;

    private AtomicLong requestTimesCounter = new AtomicLong(0);

    private volatile long lastRequestTime = System.currentTimeMillis();

    private volatile long firstRequestTime = System.currentTimeMillis();

    private volatile long requestTimes;

    private long maxInactiveInterval;

    @Override
    public long getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(long maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public LocalUserToken(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public LocalUserToken() {
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public long getRequestTimes() {
        return requestTimesCounter.get();
    }

    @Override
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    @Override
    public long getSignInTime() {
        return firstRequestTime;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public TokenState getState() {
        if (state == TokenState.normal) {
            checkExpired();
        }
        return state;
    }

    @Override
    public boolean checkExpired() {
        if (UserToken.super.checkExpired()) {
            setState(TokenState.expired);
            return true;
        }
        return false;
    }

    public void setState(TokenState state) {
        this.state = state;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setFirstRequestTime(long firstRequestTime) {
        this.firstRequestTime = firstRequestTime;
    }

    public void setLastRequestTime(long lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }

    public void setRequestTimes(long requestTimes) {
        this.requestTimes = requestTimes;
        requestTimesCounter.set(requestTimes);
    }

    public void touch() {
        requestTimesCounter.addAndGet(1);
        lastRequestTime = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalUserToken copy() {
        LocalUserToken userToken = new LocalUserToken();
        userToken.firstRequestTime = firstRequestTime;
        userToken.lastRequestTime = lastRequestTime;
        userToken.requestTimesCounter = new AtomicLong(requestTimesCounter.get());
        userToken.token = token;
        userToken.userId = userId;
        userToken.state = state;
        userToken.maxInactiveInterval = maxInactiveInterval;
        userToken.type = type;
        return userToken;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && hashCode() == obj.hashCode();
    }
}
