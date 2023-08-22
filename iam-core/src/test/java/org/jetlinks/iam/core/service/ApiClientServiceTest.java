package org.jetlinks.iam.core.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenReactiveAuthenticationSupplier;
import org.hswebframework.web.crud.web.ResponseMessage;
import org.jetlinks.iam.core.command.GetApiClient;
import org.jetlinks.iam.core.command.GetWebsocketClient;
import org.jetlinks.iam.core.command.NotifySsoCommand;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.MenuEntity;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.entity.OAuth2AccessToken;
import org.jetlinks.iam.core.entity.UserDetail;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/21
 */
public class ApiClientServiceTest {

    @Test
    public void test() {
        UserTokenManager userTokenManager = Mockito.mock(UserTokenManager.class);
        Mockito.when(userTokenManager.signIn(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyLong(),
                Mockito.any(Authentication.class)))
               .thenReturn(Mono.empty());

        ApiClientConfig config = initConfig();

        UserTokenReactiveAuthenticationSupplier authSupplier = Mockito.mock(UserTokenReactiveAuthenticationSupplier.class);

        MenuService menuService = Mockito.mock(MenuService.class);
        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setOwner("sdk");
        Mockito.when(menuService.getAllMenu()).thenReturn(Collections.singletonList(menuEntity));
        PermissionCodec codec = new DefaultPermissionCodec(menuService);

        ApiClientSsoService ssoService = new ApiClientSsoService(
                userTokenManager, new UserRequestSender(), config, authSupplier, codec);

        WebClient.Builder clientBuilder = Mockito.mock(WebClient.Builder.class);
        Mockito.when(clientBuilder.clone()).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.baseUrl(Mockito.anyString())).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.filter(Mockito.any(ExchangeFilterFunction.class))).thenReturn(clientBuilder);
        WebClient webClient = getWebClient("request_token.json", new TypeReference<OAuth2AccessToken>() {
        });
        Mockito.when(clientBuilder.build()).thenReturn(webClient);

        ApiClientService apiClientService = new ApiClientService(ssoService, clientBuilder, config);

        apiClientService
                .execute(new NotifySsoCommand(new HashMap<>()))
                .as(StepVerifier::create)
                .expectNextMatches(ssoResult -> "sdk-test".equals(ssoResult.getName()))
                .verifyComplete();

        apiClientService
                .execute(new GetApiClient())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        apiClientService
                .execute(new GetWebsocketClient())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    private ApiClientConfig initConfig() {
        ApiClientConfig config = new ApiClientConfig();
        config.setClientApiPath("http://127.0.0.1:8080");
        config.setServerApiPath("http://127.0.0.1:9000/api");
        config.setClientId("test");
        config.setClientSecret("test");
        config.setRedirectUri("http://127.0.0.1:8080");
        config.setAuthorizationUrl("http://127.0.0.1:9000/#/oauth");
        return config;
    }

    @SuppressWarnings("all")
    private WebClient getWebClient(String responseResourcePath, TypeReference type) {
        WebClient webClient = Mockito.mock(WebClient.class);
        WebClient.RequestHeadersUriSpec headersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec resp = Mockito.mock(WebClient.ResponseSpec.class);
        Mockito.when(webClient.get()).thenReturn(headersUriSpec);
        Mockito.when(headersUriSpec.uri(Mockito.any(Function.class))).thenReturn(headersUriSpec);
        Mockito.when(headersUriSpec.uri(Mockito.anyString())).thenReturn(headersUriSpec);
        Mockito.when(headersUriSpec.headers(Mockito.any(Consumer.class))).thenReturn(headersUriSpec);
        Mockito.when(headersUriSpec.retrieve()).thenReturn(resp);

        WebClient.RequestBodyUriSpec bodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        Mockito.when(webClient.post()).thenReturn(bodyUriSpec);
        Mockito.when(bodyUriSpec.uri(Mockito.any(Function.class))).thenReturn(bodyUriSpec);
        Mockito.when(bodyUriSpec.uri(Mockito.anyString())).thenReturn(bodyUriSpec);
        Mockito.when(bodyUriSpec.headers(Mockito.any(Consumer.class))).thenReturn(bodyUriSpec);
        Mockito.when(bodyUriSpec.contentType(Mockito.any(MediaType.class))).thenReturn(bodyUriSpec);
        Mockito.when(bodyUriSpec.body(Mockito.any())).thenReturn(headersUriSpec);

        Mockito.when(resp.onStatus(Mockito.any(Predicate.class), Mockito.any(Function.class))).thenReturn(resp);

        ResponseMessage userDetail = ResponseMessage.ok(getResponseMessage("user_detail.json", new TypeReference<UserDetail>() {
        }));
        ResponseMessage userMenu = ResponseMessage.ok(getResponseMessage("user_menu.json", new TypeReference<List<MenuView>>() {
        }));
        ResponseMessage userAuth = ResponseMessage.ok(getResponseMessage("user_auth.json", new TypeReference<Map<String, Object>>() {
        }));
        Object token = getResponseMessage("request_token.json", new TypeReference<OAuth2AccessToken>() {
        });
        Mockito.when(resp.bodyToMono(Mockito.any(ParameterizedTypeReference.class)))
               .thenAnswer(invocation -> {
                   Object arg = invocation.getArguments()[0];
                   if (arg instanceof ParameterizedTypeReference) {
                       String typeName = ((ParameterizedTypeReference<?>) arg).getType().getTypeName();
                       if (typeName.contains("UserDetail")) {
                           return Mono.just(userDetail);
                       }
                       if (typeName.contains("MenuView")) {
                           return Mono.just(userMenu);
                       }
                       return Mono.just(userAuth);
                   }
                   if (arg instanceof Class) {
                       return Mono.just(userAuth);
                   }
                   return Mono.just(userAuth);
               });
        Mockito.when(resp.bodyToMono(Mockito.any(Class.class))).thenReturn(Mono.just(token));
        return webClient;
    }

    private Object getResponseMessage(String responseResourcePath, TypeReference type) {
        InputStream inputStream = null;
        String json = null;
        try {
            inputStream = new ClassPathResource(responseResourcePath).getInputStream();
            json = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONObject.parseObject(json, type);
    }

}
