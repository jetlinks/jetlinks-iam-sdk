package org.jetlinks.iam.core.request;

import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.crud.web.ResponseMessage;
import org.hswebframework.web.exception.BusinessException;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.service.PermissionCodec;
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

    private final PermissionCodec permissionCodec;

    public UserMenuRequest(String clientId, String token, WebClient client, PermissionCodec permissionCodec) {
        super(token, client);
        this.clientId = clientId;
        this.permissionCodec = permissionCodec;
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
                .doOnNext(this::decodeMenu)
                .flatMapIterable(Function.identity());
    }

    private void decodeMenu(List<MenuView> menuList) {
        for (MenuView menu : menuList) {
            if (CollectionUtils.isNotEmpty(menu.getChildren())) {
                decodeMenu(menu.getChildren());
            }
            menu.decodePermission(permissionCodec);
        }
    }
}
