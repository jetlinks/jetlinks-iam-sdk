package org.jetlinks.iam.sdk.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.jetlinks.iam.sdk.service.SsoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

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
    public Mono<Void> handleGetNotify(HttpServletRequest request, HttpServletResponse response) {
        return ssoService
                .handleSsoNotify(toSingleMap(request.getParameterMap()), response);
    }

    @PostMapping("/notify")
    @Operation(summary = "(POST)登录结果通知并跳转页面")
    public Mono<Void> handlePostNotify(HttpServletRequest request, HttpServletResponse response) {
        return ssoService
                .handleSsoNotify(toSingleMap(request.getParameterMap()), response);
    }

    private Map<String, String> toSingleMap(Map<String, String[]> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>(parameterMap.size());
        parameterMap.forEach((key, value) -> map.put(key, value == null || value.length == 0 ? "" : value[0]));
        return map;
    }

}
