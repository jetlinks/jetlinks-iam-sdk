package org.jetlinks.iam.examples.spring.mvc.server.interceptor;

import org.jetlinks.iam.core.token.TokenState;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/7
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnAuthorizedException extends RuntimeException{
    private static final long serialVersionUID = 2422918455013900645L;

    private final TokenState state;

    public UnAuthorizedException() {
        this(TokenState.expired);
    }

    public UnAuthorizedException(TokenState state) {
        this(state.getText(), state);
    }

    public UnAuthorizedException(String message, TokenState state) {
        super(message);
        this.state = state;
    }

    public UnAuthorizedException(String message, TokenState state, Throwable cause) {
        super(message, cause);
        this.state = state;
    }

    public TokenState getState() {
        return state;
    }
}
