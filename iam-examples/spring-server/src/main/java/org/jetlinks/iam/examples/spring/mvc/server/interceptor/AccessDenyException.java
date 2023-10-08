package org.jetlinks.iam.examples.spring.mvc.server.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/7
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDenyException extends RuntimeException {

    private static final long serialVersionUID = -5135300127303801430L;

    public AccessDenyException() {
        this("error.access_denied");
    }

    public AccessDenyException(String message) {
        super(message);
    }

    public AccessDenyException(String permission, Set<String> actions) {
        super("permission_denied. permission: " + permission + ", actions: " + actions);
    }

    public AccessDenyException(String message, Throwable cause) {
        super(message, cause);
    }
}
