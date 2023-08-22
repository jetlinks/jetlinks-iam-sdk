package org.jetlinks.iam.core.request;

import org.hswebframework.web.crud.web.ResponseMessage;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.iam.core.entity.UserDetail;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 查询用户详情.
 *
 * @author zhangji 2023/8/10
 */
public class UserDetailRequest extends ApiRequest<Mono<UserDetail>> {


    public UserDetailRequest(String token, WebClient client) {
        super(token, client);
    }

    @Override
    public Mono<UserDetail> execute() {
        return getClient()
                .get()
                .uri("/user/detail")
                .headers(headers -> headers.setBearerAuth(getToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseMessage<UserDetail>>() {
                })
                .mapNotNull(msg -> {
                    if (msg.getStatus() != 200) {
                        throw new BusinessException(msg.getMessage());
                    }
                    if (msg.getResult() == null) {
                        throw new BusinessException("查询用户信息失败");
                    }
                    return msg.getResult();
                });
    }
}
