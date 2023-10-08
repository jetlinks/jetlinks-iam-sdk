package org.jetlinks.iam.core.service;

import org.jetlinks.iam.core.command.NotifySsoCommand;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.entity.OAuth2AccessToken;
import org.jetlinks.iam.core.entity.SsoResult;
import org.jetlinks.iam.core.request.AuthenticationRequest;
import org.jetlinks.iam.core.request.UserDetailRequest;
import org.jetlinks.iam.core.token.AppUserTokenManager;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 单点登录服务.
 *
 * @author zhangji 2023/8/4
 */
public class ApiClientSsoService {

    private final AppUserTokenManager userTokenManager;

    private final UserRequestSender userService;

    private final ApiClientConfig config;

    public ApiClientSsoService(AppUserTokenManager userTokenManager,
                               UserRequestSender userService,
                               ApiClientConfig config) {
        this.userTokenManager = userTokenManager;
        this.userService = userService;
        this.config = config;
    }

    /**
     * 处理单点登录回调
     *
     * @param command      命令
     * @param restTemplate WebClient
     * @return 回调结果
     */
    public Mono<SsoResult> handleSsoNotify(NotifySsoCommand command, RestTemplate restTemplate) {
        return this
                .requestToken(command, restTemplate)
                // 获取用户详情
                .flatMap(token -> Mono
                        .fromSupplier(() -> userService
                                .execute(new UserDetailRequest(token.getAccessToken(), restTemplate)))
                        .map(SsoResult::of)
                        .map(result -> {
                            token.setUserId(result.getUserId());
                            return result.with(token);
                        }))
                // 获取用户权限
                .flatMap(result -> Mono
                        .fromSupplier(() -> userService
                                .execute(new AuthenticationRequest(
                                        config.getClientId(), result.getToken().getAccessToken(), restTemplate)
                                ))
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
        return Mono.defer(() -> {
            userTokenManager
                    .signIn(
                            token,
                            authentication.getUser().getId(),
                            expiresMillis == null ? 7200_000 : expiresMillis,
                            authentication
                    );
            return Mono.empty();
        });
    }

    /**
     * 注销用户
     *
     * @param userId 用户ID
     * @return Void
     */
    public Mono<Void> signOut(String userId) {
        return Mono.defer(() -> {
            userTokenManager.signOutByUserId(userId);
            return Mono.empty();
        });
    }

    /**
     * 向服务器请求token
     *
     * @param command      命令
     * @param restTemplate WebClient
     * @return 单点登录token
     */
    private Mono<OAuth2AccessToken> requestToken(NotifySsoCommand command, RestTemplate restTemplate) {
        return Mono
                .fromSupplier(() -> {
                    Map<String, String> parameter = command.getParameter();

                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

                    MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
                    request.add("client_id", config.getClientId());
                    request.add("client_secret", config.getClientSecret());
                    request.add("code", String.valueOf(parameter.getOrDefault("code", "")));
                    request.add("state", String.valueOf(parameter.getOrDefault("state", "")));
                    request.add("grant_type", "authorization_code");
                    request.add("redirect_uri", config.getRedirectUri());
                    request.add("oauth_timestamp", String.valueOf(System.currentTimeMillis()));

                    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity(request, httpHeaders);
                    return restTemplate.exchange(config.getTokenRequestUrl(), HttpMethod.POST, httpEntity, OAuth2AccessToken.class);
                })
                .flatMap(response -> {
                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        return Mono.just(response.getBody());
                    } else {
                        return Mono.error(new RuntimeException("获取oauth2 token失败"));
                    }
                });
    }
}
