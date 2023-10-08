package org.jetlinks.iam.core.request;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.iam.core.command.Command;
import org.springframework.web.client.RestTemplate;

/**
 * 应用请求定义.
 *
 * @author zhangji 2023/8/10
 */
@Getter
@Setter
public abstract class ApiRequest<T> implements Command<T> {

    private String token;

    private RestTemplate restTemplate;

    public ApiRequest(String token, RestTemplate restTemplate) {
        this.token = token;
        this.restTemplate = restTemplate;
    }

    public abstract T execute();

}