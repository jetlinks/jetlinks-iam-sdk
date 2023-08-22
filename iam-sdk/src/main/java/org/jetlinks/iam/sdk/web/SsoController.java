package org.jetlinks.iam.sdk.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.jetlinks.iam.sdk.service.SsoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 单点登录回调接口.
 *
 * @author zhangji 2023/8/3
 */
@AllArgsConstructor
@RestController
@Tag(name = "单点登录回调接口")
@RequestMapping("/application/sso")
public class SsoController {

    private final SsoService ssoService;

    @GetMapping("/notify")
    @Operation(summary = "登录结果通知并跳转页面")
    public Mono<Void> handleGetNotify(ServerWebExchange exchange) {
        return ssoService
                .handleSsoNotify(exchange, exchange.getRequest().getQueryParams().toSingleValueMap());
    }

    @PostMapping("/notify")
    @Operation(summary = "(POST)登录结果通知并跳转页面")
    public Mono<Void> handlePostNotify(ServerWebExchange exchange) {
        return ssoService
                .handleSsoNotify(exchange, exchange.getRequest().getQueryParams().toSingleValueMap());
    }

}
