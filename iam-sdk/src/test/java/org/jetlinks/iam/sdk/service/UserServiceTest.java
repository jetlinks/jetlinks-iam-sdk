package org.jetlinks.iam.sdk.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.crud.web.ResponseMessage;
import org.jetlinks.iam.core.command.GetApiClient;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.MenuEntity;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.entity.UserDetail;
import org.jetlinks.iam.core.service.*;
import org.jetlinks.iam.core.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 用户服务-单元测试.
 *
 * @author zhangji 2023/8/21
 */
public class UserServiceTest {

    private DefaultServerWebExchange exchange;

    @BeforeEach
    public void init() {
        ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.put(TokenUtils.TOKEN_HEADER, Collections.singletonList("test"));
        Mockito.when(request.getHeaders()).thenReturn(headers);

        exchange = Mockito.mock(DefaultServerWebExchange.class);
        Mockito.when(exchange.getRequest()).thenReturn(request);
    }

    @Test
    public void testUserAuth() {
        UserService userService = getUserService("user_auth.json", new TypeReference<Map<String, Object>>() {
        });

        userService
                .getCurrentAuthentication(exchange)
                .map(auth -> auth.getPermissions().get(0).getId())
                .as(StepVerifier::create)
                // 解码正常: device@sdk -> device
                .expectNext("device")
                .verifyComplete();
    }

    @Test
    public void testUserDetail() {
        UserService userService = getUserService("user_detail.json", new TypeReference<UserDetail>() {
        });

        userService
                .getCurrentUserDetail(exchange)
                .as(StepVerifier::create)
                .expectNextMatches(userDetail -> "sdk-test".equals(userDetail.getName()))
                .verifyComplete();
    }

    @Test
    public void testUserMenu() {
        UserService userService = getUserService("user_menu.json", new TypeReference<List<MenuView>>() {
        });

        userService
                .getCurrentMenu(exchange)
                .map(menuView -> menuView.getChildren().get(0).getAssetType())
                .as(StepVerifier::create)
                // 解码正常: device@sdk -> device
                .expectNext("device")
                .verifyComplete();
    }

    private UserService getUserService(String responseResourcePath, TypeReference type) {
        ApiClientConfig config = new ApiClientConfig();

        ApiClientSsoService ssoService = Mockito.mock(ApiClientSsoService.class);
        Mockito
                .when(ssoService.signIn(Mockito.anyString(), Mockito.any(Authentication.class), Mockito.nullable(Long.class)))
                .thenReturn(Mono.empty());

        ApiClientService apiClientService = Mockito.mock(ApiClientService.class);
        WebClient client = getWebClient(responseResourcePath, type);
        Mockito.when(apiClientService.execute(Mockito.any(GetApiClient.class)))
               .thenReturn(Mono.just(client));

        MenuService menuService = Mockito.mock(MenuService.class);
        MenuEntity menuEntity = new MenuEntity();
        Mockito.when(menuService.getAllMenu()).thenReturn(Collections.singletonList(menuEntity));

        PermissionCodec codec = new DefaultPermissionCodec("sdk", menuService);

        return new UserService(
                config, new UserRequestSender(), ssoService, apiClientService, codec
        );
    }

    @SuppressWarnings("all")
    private WebClient getWebClient(String responseResourcePath, TypeReference type) {
        WebClient webClient = Mockito.mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uri = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec resp = Mockito.mock(WebClient.ResponseSpec.class);
        Mockito.when(webClient.get()).thenReturn(uri);
        Mockito.when(uri.uri(Mockito.any(Function.class))).thenReturn(uri);
        Mockito.when(uri.uri(Mockito.anyString())).thenReturn(uri);
        Mockito.when(uri.headers(Mockito.any(Consumer.class))).thenReturn(uri);
        Mockito.when(uri.retrieve()).thenReturn(resp);
        Mockito.when(resp.onStatus(Mockito.any(Predicate.class), Mockito.any(Function.class))).thenReturn(resp);
        InputStream inputStream = null;
        String json = null;
        try {
            inputStream = new ClassPathResource(responseResourcePath).getInputStream();
            json = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResponseMessage responseMessage = ResponseMessage.ok(JSONObject.parseObject(json, type));
        Mockito.when(resp.bodyToMono(Mockito.any(ParameterizedTypeReference.class)))
               .thenReturn(Mono.just(responseMessage));
        return webClient;
    }
}
