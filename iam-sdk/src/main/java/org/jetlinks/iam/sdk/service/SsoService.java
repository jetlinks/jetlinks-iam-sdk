package org.jetlinks.iam.sdk.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.RandomUtil;
import org.jetlinks.iam.core.command.NotifySsoCommand;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.service.ApiClientService;
import org.jetlinks.iam.core.service.SsoHandler;
import org.jetlinks.core.message.codec.http.HttpUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 单点登录服务.
 *
 * @author zhangji 2023/8/1
 */
@Slf4j
public class SsoService {

    private final ApiClientService apiClientService;

    private final ApiClientConfig config;

    private final List<SsoHandler> ssoHandlers = new ArrayList<>();

    public SsoService(ApiClientService apiClientService,
                      ApiClientConfig config,
                      ObjectProvider<SsoHandler> objectProvider) {
        this.apiClientService = apiClientService;
        this.config = config;

        objectProvider.forEach(ssoHandlers::add);
    }

    /**
     * @param parameters 自定义请求参数
     * @return 单点登录地址
     */
    public Mono<URI> requestSsoUri(Map<String, String> parameters) {
        String state = RandomUtil.randomChar(6);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(config.getClientApiPath())
                .path("/application/sso/notify");
        parameters.forEach((k, v) -> builder.queryParam(k, HttpUtils.urlEncode(v)));

        return Mono
                .just(
                        UriComponentsBuilder
                                .fromUriString(config.getAuthorizationUrl())
                                .queryParam("redirect_uri", HttpUtils.urlEncode(builder.build().toUriString()))
                                .queryParam("client_id", config.getClientId())
                                .queryParam("scope", "*")
                                .queryParam("state", state)
                                .queryParam("response_type", "code")
                                //标识为内部应用
                                .queryParam("internal", "true")
                                .build(true)
                                .toUri()
                );
    }

    /**
     * 处理登录后的回调
     * @param exchange 请求
     * @param parameter 请求参数
     * @return Void
     */
    public Mono<Void> handleSsoNotify(ServerWebExchange exchange,
                                      Map<String, String> parameter) {

        String redirect = parameter.get("redirect");
        if (null == redirect) {
            redirect = config.getRedirectUri();
        }
        String sourceRedirect = redirect;

        return apiClientService
                .execute(new NotifySsoCommand(parameter))
                .doOnNext(result -> ssoHandlers.forEach(ssoHandler -> ssoHandler.handleResult(result)))
                .doOnNext(result -> {
                    //重定向到设置token的地址
                    URI uri = UriComponentsBuilder
                            .fromUriString(config.getTokenSetUrl())
                            .queryParam("sso", "true")
                            .queryParam("token", result.getToken().getAccessToken())
                            .queryParam("redirect", HttpUtils.urlEncode(sourceRedirect))
                            .build(true)
                            .toUri();
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(uri);
                })
                .then();
    }

}
