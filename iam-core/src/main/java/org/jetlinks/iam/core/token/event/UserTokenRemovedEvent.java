package org.jetlinks.iam.core.token.event;

import org.jetlinks.iam.core.token.UserToken;
import org.springframework.context.ApplicationEvent;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
public class UserTokenRemovedEvent extends ApplicationEvent {

    private static final long serialVersionUID = -6662943150068863177L;

    public UserTokenRemovedEvent(UserToken token) {
        super(token);
    }

    public UserToken getDetail() {
        return ((UserToken) getSource());
    }
}
