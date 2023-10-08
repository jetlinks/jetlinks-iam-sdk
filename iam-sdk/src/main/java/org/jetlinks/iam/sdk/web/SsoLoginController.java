package org.jetlinks.iam.sdk.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.ResponseMessage;
import org.jetlinks.iam.sdk.service.SsoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/14
 */
@RestController
@AllArgsConstructor
@Tag(name = "用户单点登录接口")
@RequestMapping("${jetlinks.api.client.config.sso-login-url:/api/application/sso}")
public class SsoLoginController {

    private final ApiClientConfig config;

    private final SsoService ssoService;

    @GetMapping("/{appId}/login")
    @Operation(summary = "跳转到单点登陆页面")
    public Mono<Void> redirectSsoLogin(@PathVariable @Parameter(description = "应用ID") String appId,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        return getLoginUri(appId, request)
                .doOnNext(url -> {
                    //重定向到登录地址
                    response.setStatus(HttpStatus.FOUND.value());
                    response.setHeader("Location", url.getResult().toString());
                })
                .then();
    }

    @GetMapping("/{appId}/login/url")
    @Operation(summary = "获取应用单点登陆地址")
    public Mono<ResponseMessage<URI>> getLoginUri(@PathVariable @Parameter(description = "应用ID") String appId,
                                                  HttpServletRequest request) {
        if (!config.getClientId().equals(appId)) {
            return Mono.error(new UnsupportedOperationException("应用ID错误:" + appId));
        }
        return ssoService
                .requestSsoUri(request.getQueryString())
                .map(ResponseMessage::ok);
    }
}
