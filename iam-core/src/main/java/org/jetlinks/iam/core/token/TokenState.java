package org.jetlinks.iam.core.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
@Getter
@AllArgsConstructor
public enum TokenState {
    /**
     * 正常，有效
     */
    normal("normal", "message.token_state_normal"),

    /**
     * 已被禁止访问
     */
    deny("deny", "message.token_state_deny"),

    /**
     * 已过期
     */
    expired("expired", "message.token_state_expired"),

    /**
     * 已被踢下线
     */
    offline("offline", "message.token_state_offline"),

    /**
     * 锁定
     */
    lock("lock", "message.token_state_lock");

    private final String value;

    private final String text;
}