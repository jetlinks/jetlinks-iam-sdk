package org.jetlinks.iam.core.request;

import org.hswebframework.web.crud.web.ResponseMessage;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.iam.core.entity.MenuView;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

/**
 * 查询用户拥有的菜单.
 *
 * @author zhangji 2023/8/14
 */
public class UserMenuRequest extends ApiRequest<Flux<MenuView>> {

    private final String clientId;

    public UserMenuRequest(String clientId, String token, WebClient client) {
        super(token, client);
        this.clientId = clientId;
    }

    @Override
    public Flux<MenuView> execute() {
        return getClient()
                .get()
                .uri("/application/" + clientId + "/menu/tree")
                .headers(headers -> headers.setBearerAuth(getToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseMessage<List<MenuView>>>() {
                })
                .mapNotNull(msg -> {
                    if (msg.getStatus() != 200) {
                        throw new BusinessException(msg.getMessage());
                    }
                    if (msg.getResult() == null) {
                        throw new BusinessException("查询用户菜单失败");
                    }
                    return msg.getResult();
                })
                .flatMapIterable(Function.identity());
    }
}
