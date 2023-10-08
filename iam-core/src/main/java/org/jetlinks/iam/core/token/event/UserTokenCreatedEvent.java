package org.jetlinks.iam.core.token.event;

import org.jetlinks.iam.core.token.UserToken;
import org.springframework.context.ApplicationEvent;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
public class UserTokenCreatedEvent extends ApplicationEvent {

    private static final long serialVersionUID = -5597388797881241775L;

    private final UserToken detail;

    public UserTokenCreatedEvent(UserToken detail) {
        super(detail);
        this.detail = detail;
    }

    public UserToken getDetail() {
        return detail;
    }
}
