package org.jetlinks.iam.sdk.service;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.jetlinks.iam.core.command.GetApiClient;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.entity.UserDetail;
import org.jetlinks.iam.core.request.AuthenticationRequest;
import org.jetlinks.iam.core.request.UserDetailRequest;
import org.jetlinks.iam.core.request.UserMenuRequest;
import org.jetlinks.iam.core.service.ApiClientService;
import org.jetlinks.iam.core.service.ApiClientSsoService;
import org.jetlinks.iam.core.service.PermissionCodec;
import org.jetlinks.iam.core.service.UserRequestSender;
import org.jetlinks.iam.core.utils.TokenUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户服务.
 *
 * @author zhangji 2023/8/10
 */
public class UserService {

    private final ApiClientConfig config;

    private final UserRequestSender sender;

    private final ApiClientSsoService apiClientSsoService;

    private final Mono<WebClient> clientMono;

    private final PermissionCodec permissionCodec;

    public UserService(ApiClientConfig config,
                       UserRequestSender sender,
                       ApiClientSsoService apiClientSsoService,
                       ApiClientService apiClientService,
                       PermissionCodec permissionCodec) {
        this.config = config;
        this.sender = sender;
        this.apiClientSsoService = apiClientSsoService;
        this.clientMono = apiClientService.execute(new GetApiClient());
        this.permissionCodec = permissionCodec;
    }

    /**
     * 查询当前用户信息
     *
     * @param exchange 请求
     * @return 用户信息
     */
    public Mono<UserDetail> getCurrentUserDetail(ServerWebExchange exchange) {
        return Mono
                .zip(parseToken(exchange), clientMono)
                .flatMap(tp2 -> sender.execute(new UserDetailRequest(tp2.getT1().getToken(), tp2.getT2())));
    }

    /**
     * 查询当前用户菜单
     *
     * @param exchange 请求
     * @return 菜单
     */
    public Flux<MenuView> getCurrentMenu(ServerWebExchange exchange) {
        return Mono
                .zip(parseToken(exchange), clientMono)
                .flatMapMany(tp2 -> sender.execute(new UserMenuRequest(
                        config.getClientId(), tp2.getT1().getToken(), tp2.getT2(), permissionCodec
                )));
    }

    /**
     * 查询当前用户权限
     *
     * @param exchange 请求
     * @return 权限
     */
    public Mono<Authentication> getCurrentAuthentication(ServerWebExchange exchange) {
        return Mono
                .zip(parseToken(exchange), clientMono)
                .flatMap(tp2 -> sender
                        .execute(new AuthenticationRequest(tp2.getT1().getToken(), tp2.getT2(), permissionCodec))
                        .flatMap(authentication -> apiClientSsoService
                                .signIn(tp2.getT1().getToken(), authentication, null)
                                .thenReturn(authentication)));
    }

    private Mono<ParsedToken> parseToken(ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> TokenUtils.parseTokenHeader(exchange.getRequest()));
    }
}
