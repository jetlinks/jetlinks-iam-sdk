package org.jetlinks.iam.sdk.web;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.Parameter;
import org.jetlinks.iam.core.entity.ResponseMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 用户授权接口.
 *
 * @author zhangji 2023/8/14
 */
@RestController
@AllArgsConstructor
@Tag(name = "客户端应用接口")
@RequestMapping("/client/config")
public class ClientConfigController {

    private final ApiClientConfig config;

    @GetMapping
    @Operation(summary = "查询客户端应用配置")
    public ResponseMessage<ConfigInfo> getClientConfig() {
        return ResponseMessage.ok(ConfigInfo.of(config));
    }

    @Getter
    @Setter
    public static class ConfigInfo {
        // 当前服务-接口地址
        private String clientApiPath;

        // 用户中台-接口地址
        private String serverApiPath;

        // 应用ID
        private String clientId;

        // 授权地址
        private String authorizationUrl;

        // 授权后的重定向地址
        private String redirectUri;

        // 请求token地址
        private String tokenRequestUrl;

        // 设置token地址，默认为：客户端地址 + /token-set.html
        private String tokenSetUrl;

        // 请求头
        private List<Parameter> headers;

        // 请求参数
        private List<Parameter> parameters;

        // 菜单接口请求地址，默认为/api/menu
        private String menuUrl;

        public static ConfigInfo of(ApiClientConfig config) {
            return JSONObject.parseObject(JSONObject.toJSONString(config), ConfigInfo.class);
        }
    }

}
