package org.jetlinks.iam.core.token.event;

import org.jetlinks.iam.core.token.UserToken;
import org.springframework.context.ApplicationEvent;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
public class UserTokenChangedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 4920356324808918056L;

    private final UserToken before, after;

    public UserTokenChangedEvent(UserToken before, UserToken after) {
        super(after);
        this.before = before;
        this.after = after;
    }

    public UserToken getBefore() {
        return before;
    }

    public UserToken getAfter() {
        return after;
    }
}