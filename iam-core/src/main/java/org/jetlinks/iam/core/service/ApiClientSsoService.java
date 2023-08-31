package org.jetlinks.iam.core.service;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenReactiveAuthenticationSupplier;
import org.jetlinks.iam.core.command.NotifySsoCommand;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.OAuth2AccessToken;
import org.jetlinks.iam.core.entity.SsoResult;
import org.jetlinks.iam.core.request.AuthenticationRequest;
import org.jetlinks.iam.core.request.UserDetailRequest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;

/**
 * 单点登录服务.
 *
 * @author zhangji 2023/8/4
 */
public class ApiClientSsoService {

    private final UserTokenManager userTokenManager;

    private final UserRequestSender userService;

    private final ApiClientConfig config;

    private final UserTokenReactiveAuthenticationSupplier authenticationSupplier;

    private final PermissionCodec permissionCodec;

    public ApiClientSsoService(UserTokenManager userTokenManager,
                               UserRequestSender userService,
                               ApiClientConfig config,
                               UserTokenReactiveAuthenticationSupplier authenticationSupplier,
                               PermissionCodec permissionCodec) {
        this.userTokenManager = userTokenManager;
        this.userService = userService;
        this.config = config;
        this.authenticationSupplier = authenticationSupplier;
        this.permissionCodec = permissionCodec;
    }

    /**
     * 处理单点登录回调
     *
     * @param command 命令
     * @param client  WebClient
     * @return 回调结果
     */
    public Mono<SsoResult> handleSsoNotify(NotifySsoCommand command, WebClient client) {
        return this
                .requestToken(command, client)
                // 获取用户详情
                .flatMap(token -> userService
                        .execute(new UserDetailRequest(token.getAccessToken(), client))
                        .map(SsoResult::of)
                        .map(result -> {
                            token.setUserId(result.getUserId());
                            return result.with(token);
                        }))
                // 获取用户权限
                .flatMap(result -> userService
                        .execute(new AuthenticationRequest(
                                config.getClientId(), result.getToken().getAccessToken(), client, permissionCodec)
                        )
                        // 使用此token登录当前用户
                        .flatMap(authentication -> this.signIn(
                                result.getToken().getAccessToken(), authentication, result.getExpiresMillis()
                        ))
                        .thenReturn(result));
    }

    /**
     * 登录用户
     *
     * @param token          用户token
     * @param authentication 用户权限信息
     * @param expiresMillis  超时时间（毫秒）
     * @return Void
     */
    public Mono<Void> signIn(String token, Authentication authentication, Long expiresMillis) {
        return userTokenManager
                .signIn(
                        token,
                        "sso",
                        authentication.getUser().getId(),
                        expiresMillis == null ? 7200_000 : expiresMillis,
                        authentication
                )
                .flatMap(userToken -> authenticationSupplier
                        .get()
                        .contextWrite(Context.of(
                                ParsedToken.class,
                                ParsedToken.of(userToken.getType(), userToken.getToken())
                        )))
                .then();
    }

    /**
     * 注销用户
     *
     * @param userId 用户ID
     * @return Void
     */
    public Mono<Void> signOut(String userId) {
        return userTokenManager.signOutByUserId(userId);
    }

    /**
     * 向服务器请求token
     *
     * @param command 命令
     * @param client  WebClient
     * @return 单点登录token
     */
    private Mono<OAuth2AccessToken> requestToken(NotifySsoCommand command, WebClient client) {
        Map<String, String> parameter = command.getParameter();

        String code = String.valueOf(parameter.getOrDefault("code", ""));
        String state = String.valueOf(parameter.getOrDefault("state", ""));

        return client
                .post()
                .uri(config.getTokenRequestUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                              .fromFormData("client_id", config.getClientId())
                              .with("client_secret", config.getClientSecret())
                              .with("code", code)
                              .with("state", state)
                              .with("grant_type", "authorization_code")
                              .with("redirect_uri", config.getRedirectUri())
                              .with("oauth_timestamp", String.valueOf(System.currentTimeMillis()))
                )
                .retrieve()
                .bodyToMono(OAuth2AccessToken.class);
    }
}
