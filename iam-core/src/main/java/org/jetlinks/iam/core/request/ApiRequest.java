package org.jetlinks.iam.core.request;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.crud.web.ResponseMessage;
import org.jetlinks.core.command.Command;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 应用请求定义.
 *
 * @author zhangji 2023/8/10
 */
@Getter
@Setter
public abstract class ApiRequest<T> implements Command<T> {

    private String token;

    private WebClient client;

    public ApiRequest(String token, WebClient client) {
        this.token = token;
        this.client = client;
    }

    public abstract T execute();

}