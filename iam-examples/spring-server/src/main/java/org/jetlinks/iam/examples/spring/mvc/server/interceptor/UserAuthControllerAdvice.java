package org.jetlinks.iam.examples.spring.mvc.server.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.iam.core.entity.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/7
 */
@Slf4j
@RestControllerAdvice
public class UserAuthControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseMessage<Object> handleException(UnAuthorizedException e) {
        log.error(e.getLocalizedMessage(), e);
        return ResponseMessage.error(500, "error." + e.getClass().getSimpleName(), e.getLocalizedMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseMessage<Object> handleException(AccessDenyException e) {
        log.error(e.getLocalizedMessage(), e);
        return ResponseMessage.error(500, "error." + e.getClass().getSimpleName(), e.getLocalizedMessage());
    }
}
