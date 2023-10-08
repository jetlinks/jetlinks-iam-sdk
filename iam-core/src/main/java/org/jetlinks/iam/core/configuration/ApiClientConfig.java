package org.jetlinks.iam.core.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.iam.core.entity.OAuth2AccessToken;
import org.jetlinks.iam.core.entity.Parameter;
import org.jetlinks.iam.core.filter.InternalTokenExchangeCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.Duration;
import java.util.List;

/**
 * 客户端应用配置.
 * <p>
 * 对应JetLinks-应用管理中的配置
 * 示例如下：
 * <code>
 * <p>
 * jetlinks:
 *   api:
 *     client:
 *       config:
 *         client-api-path: http://127.0.0.1:8080  #当前服务-接口地址
 *         server-api-path: http://127.0.0.1:9000/api #用户中台-接口地址
 *         client-id: Ncj4hBQNAW7xPPSS #应用ID
 *         client-secret: cmB3NRwEHTfyB7GJwm873rj3AB7S7AnY #应用密钥
 *         redirect-uri: http://127.0.0.1:8080 # 授权后的重定向地址
 *         authorization-url: http://127.0.0.1:9000/#/oauth
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

    // 连接超时时间（毫秒）
    private long timeoutMills = 5000;

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

    public OAuth2AccessToken refresh(OAuth2AccessToken accessToken,
                                     RestTemplate restTemplate) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("client_id", clientId);
        request.add("client_secret", clientSecret);
        request.add("refresh_token", accessToken.getRefreshToken());
        request.add("grant_type", "refresh_token");
        request.add("redirect_uri", clientApiPath + redirectUri);
        request.add("oauth_timestamp", String.valueOf(System.currentTimeMillis()));

        ResponseEntity<String> responseEntity = requestForString(restTemplate, request);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return parseToken(responseEntity.getBody());
        } else {
            log.warn("refresh [{}] token error,{}: {}", clientId, responseEntity.getStatusCode(), responseEntity.getBody());
            return null;
        }
    }

    public OAuth2AccessToken requestToken(RestTemplate restTemplate) throws Exception {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("client_id", clientId);
        request.add("client_secret", clientSecret);
        request.add("grant_type", "client_credentials");
        request.add("redirect_uri", clientApiPath + redirectUri);
        request.add("oauth_timestamp", String.valueOf(System.currentTimeMillis()));

        ResponseEntity<String> responseEntity = requestForString(restTemplate, request);
        return handleResponse(responseEntity);

    }

    public OAuth2AccessToken requestToken(RestTemplate restTemplate, String code, String state, String redirectUri) throws Exception {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("client_id", clientId);
        request.add("client_secret", clientSecret);
        request.add("code", code);
        request.add("state", state);
        request.add("grant_type", "authorization_code");
        request.add("redirect_uri", clientApiPath + redirectUri);
        request.add("oauth_timestamp", String.valueOf(System.currentTimeMillis()));

        ResponseEntity<String> responseEntity = requestForString(restTemplate, request);
        return handleResponse(responseEntity);
    }

    private ResponseEntity<String> requestForString(RestTemplate restTemplate,
                                                    MultiValueMap<String, String> request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        ResponseEntity<String> response;
        if (tokenRequestType == RequestType.POST_BODY) {
            response = restTemplate
                    .exchange(
                            getTokenRequestUrl(),
                            HttpMethod.POST,
                            new HttpEntity<>(request, httpHeaders),
                            String.class
                    );
        } else {
            response = restTemplate
                    .exchange(
                            getTokenRequestUrl(),
                            HttpMethod.POST,
                            new HttpEntity<>(null, httpHeaders),
                            String.class,
                            request);
        }
        return response;
    }

    private OAuth2AccessToken handleResponse(ResponseEntity<String> response) throws Exception {
        if (response.getStatusCode().is2xxSuccessful()) {
            return parseToken(response.getBody());
        } else {
            log.warn("request oauth2 [{}] token error {} {} {}",
                     clientId,
                     getTokenRequestUrl(),
                     response.getStatusCode(),
                     response.getBody());
            throw new Exception("获取oauth2 token失败。" + response.getBody());
        }
    }

    protected String getProperty(String json, String property) {
        Object eval = JSONPath.eval(JSON.parseObject(json), property);
        return eval == null ? null : String.valueOf(eval);
    }

    protected OAuth2AccessToken parseToken(String json) {
        OAuth2AccessToken token = JSON.parseObject(json, OAuth2AccessToken.class);

        return token.getAccessToken() == null ? null : token;
    }

    public RestTemplate createWebClient(RestTemplateBuilder builder) {
        builder = builder
                .rootUri(serverApiPath)
                .setConnectTimeout(Duration.ofMillis(timeoutMills))
                .setReadTimeout(Duration.ofMillis(timeoutMills))
                .additionalInterceptors(new InternalTokenExchangeCustomizer(headers, parameters));

        return builder.build();
    }
}
