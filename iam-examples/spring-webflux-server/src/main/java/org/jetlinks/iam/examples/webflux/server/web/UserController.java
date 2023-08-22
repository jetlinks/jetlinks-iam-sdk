package org.jetlinks.iam.examples.webflux.server.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.entity.UserDetail;
import org.jetlinks.iam.sdk.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/4
 */
@RestController
@AllArgsConstructor
@Tag(name = "用户信息接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/user/detail")
    @Operation(summary = "获取当前用户信息")
    public Mono<UserDetail> getUser(ServerWebExchange exchange) {
        return userService.getCurrentUserDetail(exchange);
    }

    @GetMapping("/user/menu")
    @Operation(summary = "获取当前用户菜单")
    public Flux<MenuView> getUserMenu(ServerWebExchange exchange) {
        return userService.getCurrentMenu(exchange);
    }

    @GetMapping("/authorize/me")
    @Operation(summary = "获取当前用户权限")
    public Mono<Authentication> getUserAuth(ServerWebExchange exchange) {
        return Authentication
                .currentReactive()
                .switchIfEmpty(userService.getCurrentAuthentication(exchange));
    }

}
