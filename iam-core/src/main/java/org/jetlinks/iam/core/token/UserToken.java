package org.jetlinks.iam.core.token;

import java.io.Serializable;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
public interface UserToken extends Serializable, Comparable<UserToken> {
    /**
     * @return 用户id
     */
    String getUserId();

    /**
     * @return token
     */
    String getToken();

    /**
     * @return 请求总次数
     */
    long getRequestTimes();

    /**
     * @return 最后一次请求时间
     */
    long getLastRequestTime();

    /**
     * @return 首次请求时间
     */
    long getSignInTime();

    /**
     * @return 令牌状态
     */
    TokenState getState();

    /**
     * @return 令牌类型, 默认:default
     */
    String getType();

    /**
     * @return 会话过期时间, 单位毫秒
     */
    long getMaxInactiveInterval();

    /**
     * 检查会话是否过期
     *
     * @return 是否过期
     * @since 4.0.10
     */
    default boolean checkExpired() {
        long maxInactiveInterval = getMaxInactiveInterval();
        if (maxInactiveInterval > 0) {
            return System.currentTimeMillis() - getLastRequestTime() > maxInactiveInterval;
        }
        return false;
    }

    default boolean isNormal() {
        return getState() == TokenState.normal;
    }

    /**
     * @return 是否已过期
     */
    default boolean isExpired() {
        return getState() == TokenState.expired;
    }

    /**
     * @return 是否离线
     */
    default boolean isOffline() {
        return getState() == TokenState.offline;
    }

    default boolean isLock() {
        return getState() == TokenState.lock;
    }

    default boolean isDeny() {
        return getState() == TokenState.deny;
    }

    default boolean validate() {
        if (!isNormal()) {
            return false;
        }
        return true;
    }

    @Override
    default int compareTo(UserToken target) {
        if (target == null) {
            return 0;
        }
        return Long.compare(getSignInTime(), target.getSignInTime());
    }
}
