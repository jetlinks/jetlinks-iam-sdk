package org.jetlinks.iam.core.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.validator.ValidatorUtils;
import org.jetlinks.iam.core.entity.OAuth2AccessToken;
import org.jetlinks.iam.core.entity.Parameter;
import org.jetlinks.iam.core.filter.InternalTokenExchangeFilterFunction;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 客户端应用配置.
 * <p>
 * 对应JetLinks-应用管理中的配置
 * 示例如下：
 * <code>
 * <p>
 * jetlinks:
 * api:
 * client:
 * config:
 * client-api-path: http://127.0.0.1:8080  #当前服务-接口地址
 * server-api-path: http://127.0.0.1:9000/api #用户中台-接口地址
 * client-id: client-id #应用ID
 * client-secret: client-secret #应用密钥
 * redirect-uri: http://127.0.0.1:8080 # 授权后的重定向地址
 * authorization-url: http://127.0.0.1:9000/#/oauth
 *
 * </code>
 *
 * @author zhangji 2023/8/3
 */
@Slf4j
@ConfigurationProperties(prefix = "jetlinks.api.client.config")
@Getter
@Setter
public class ApiClientConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    // 当前服务-接口地址
    @NotBlank(message = "当前服务地址不能为空")
    private String clientApiPath;

    // 用户中台-接口地址
    @NotBlank(message = "用户中台地址不能为空")
    private String serverApiPath;

    // 应用ID
    @NotBlank(message = "应用ID不能为空")
    private String clientId;

    // 应用密钥
    @NotBlank(message = "应用密钥不能为空")
    private String clientSecret;

    // 授权地址
    @NotBlank(message = "授权地址不能为空")
    private String authorizationUrl;

    // 授权后的重定向地址
    @NotBlank(message = "重定向地址不能为空")
    private String redirectUri;

    // 请求token地址，默认为：服务端地址 + /oauth2/token
    private String tokenRequestUrl;

    // 请求token类型
    private RequestType tokenRequestType = RequestType.POST_BODY;

    // 设置token地址，默认为：客户端地址 + /token-set.html
    private String tokenSetUrl;

    // 请求头
    private List<Parameter> headers;

    // 请求参数
    private List<Parameter> parameters;

    // 菜单接口请求地址，默认为/api/menu
    private String menuUrl;

    public String getTokenSetUrl() {
        return StringUtils.hasText(tokenSetUrl) ? tokenSetUrl : clientApiPath + "/token-set.html";
    }

    public String getTokenRequestUrl() {
        return StringUtils.hasText(tokenRequestUrl) ? tokenRequestUrl : serverApiPath + "/oauth2/token";
    }

    public enum RequestType {
        POST_URI,
        POST_BODY
    }

    public Mono<OAuth2AccessToken> refresh(OAuth2AccessToken accessToken,
                                           WebClient webClient) {
        WebClient.RequestHeadersSpec<?> spec;
        if (tokenRequestType == RequestType.POST_BODY) {
            spec = webClient
                    .post()
                    .uri(getTokenRequestUrl())
                    .body(BodyInserters
                                  .fromFormData("client_id", clientId)
                                  .with("client_secret", clientSecret)
                                  .with("refresh_token", accessToken.getRefreshToken())
                                  .with("grant_type", "refresh_token")
                                  .with("redirect_uri", clientApiPath + redirectUri)
                                  .with("oauth_timestamp", String.valueOf(System.currentTimeMillis()))
                    );
        } else {
            spec = webClient
                    .post()
                    .uri(getTokenRequestUrl(), uriBuilder -> uriBuilder
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("refresh_token", accessToken.getRefreshToken())
                            .queryParam("grant_type", "refresh_token")
                            .queryParam("redirect_uri", clientApiPath + redirectUri)
                            .queryParam("oauth_timestamp", System.currentTimeMillis())
                            .build());
        }
        return spec
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response
                                .bodyToMono(String.class)
                                .mapNotNull(this::parseToken);
                    } else {
                        return response
                                .bodyToMono(String.class)
                                .doOnNext(body -> log.warn("refresh [{}] token error,{}: {}", clientId, response.statusCode(), body))
                                .then(Mono.empty());
                    }
                });
    }

    public Mono<OAuth2AccessToken> requestToken(WebClient webClient) {
        WebClient.RequestHeadersSpec<?> spec;
        if (tokenRequestType == RequestType.POST_BODY) {
            spec = webClient
                    .post()
                    .uri(getTokenRequestUrl())
                    .body(BodyInserters
                                  .fromFormData("client_id", clientId)
                                  .with("client_secret", clientSecret)
                                  .with("grant_type", "client_credentials")
                                  .with("redirect_uri", clientApiPath + redirectUri)
                                  .with("oauth_timestamp", String.valueOf(System.currentTimeMillis()))
                    );
        } else {
            spec = webClient
                    .post()
                    .uri(getTokenRequestUrl(), uriBuilder -> uriBuilder
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("redirect_uri", clientApiPath + redirectUri)
                            .queryParam("grant_type", "client_credentials")
                            .queryParam("oauth_timestamp", System.currentTimeMillis())
                            .build());
        }
        return doRequest(spec);

    }

    public Mono<OAuth2AccessToken> requestToken(WebClient webClient, String code, String state, String redirectUri) {
        WebClient.RequestHeadersSpec<?> spec;
        if (tokenRequestType == RequestType.POST_BODY) {
            spec = webClient
                    .post()
                    .uri(getTokenRequestUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                                  .fromFormData("client_id", clientId)
                                  .with("client_secret", clientSecret)
                                  .with("code", code)
                                  .with("state", state)
                                  .with("grant_type", "authorization_code")
                                  .with("redirect_uri", clientApiPath + redirectUri)
                                  .with("oauth_timestamp", String.valueOf(System.currentTimeMillis()))
                    );
        } else {
            spec = webClient
                    .post()
                    .uri(getTokenRequestUrl(), uriBuilder -> uriBuilder
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("code", code)
                            .queryParam("state", state)
                            .queryParam("grant_type", "authorization_code")
                            .queryParam("redirect_uri", clientApiPath + redirectUri)
                            .queryParam("oauth_timestamp", System.currentTimeMillis())
                            .build());
        }
        return doRequest(spec);
    }

    private Mono<OAuth2AccessToken> doRequest(WebClient.RequestHeadersSpec<?> spec) {
        return spec
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response
                                .bodyToMono(String.class)
                                .mapNotNull(this::parseToken);
                    } else {
                        return response
                                .bodyToMono(String.class)
                                .doOnNext(e -> log.warn("request oauth2 [{}] token error {} {} {}",
                                                        clientId,
                                                        getTokenRequestUrl(),
                                                        response.statusCode(),
                                                        e))
                                .then(Mono.empty());
                    }
                });
    }

    protected String getProperty(String json, String property) {
        Object eval = JSONPath.eval(JSON.parseObject(json), property);
        return eval == null ? null : String.valueOf(eval);
    }

    protected OAuth2AccessToken parseToken(String json) {
        OAuth2AccessToken token = JSON.parseObject(json, OAuth2AccessToken.class);

        return token.getAccessToken() == null ? null : token;
    }

    public void validate() {
        ValidatorUtils.tryValidate(this);
    }

    public WebClient createWebClient(WebClient.Builder builder) {
        builder = builder
                .baseUrl(serverApiPath)
                //透传token信息
                .filter(new InternalTokenExchangeFilterFunction());

        if (CollectionUtils.isNotEmpty(headers)) {
            builder = builder.defaultHeaders(headers -> {
                for (Parameter header : this.headers) {
                    if (header.getValue() != null) {
                        headers.set(header.getKey(), header.getValue());
                    }
                }
            });
        }

        if (CollectionUtils.isNotEmpty(parameters)) {
            UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromUriString(serverApiPath);
            for (Parameter parameter : parameters) {
                if (parameter.getKey() != null) {
                    componentsBuilder.queryParam(parameter.getKey(), parameter.getValue());
                }
            }

            builder = builder.uriBuilderFactory(new DefaultUriBuilderFactory(componentsBuilder));
        }

        return builder.build();
    }
}
