package org.jetlinks.iam.core.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.jetlinks.iam.core.command.GetApiClient;
import org.jetlinks.iam.core.command.GetWebsocketClient;
import org.jetlinks.iam.core.command.NotifySsoCommand;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.*;
import org.jetlinks.iam.core.token.AppUserTokenManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
        AppUserTokenManager userTokenManager = Mockito.mock(AppUserTokenManager.class);
//        Mockito.when(userTokenManager.signIn(
//                Mockito.anyString(),
//                Mockito.anyString(),
//                Mockito.anyLong(),
//                Mockito.any(Authentication.class)))
//               .thenReturn(Mono.empty());

        ApiClientConfig config = initConfig();

        MenuService menuService = Mockito.mock(MenuService.class);
        MenuEntity menuEntity = new MenuEntity();
        Mockito.when(menuService.getAllMenu()).thenReturn(Collections.singletonList(menuEntity));

        ApiClientSsoService ssoService = new ApiClientSsoService(
                userTokenManager, new UserRequestSender(), config);

        RestTemplateBuilder clientBuilder = Mockito.mock(RestTemplateBuilder.class);
        Mockito.when(clientBuilder.rootUri(Mockito.anyString())).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.setConnectTimeout(Mockito.any(Duration.class))).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.setReadTimeout(Mockito.any(Duration.class))).thenReturn(clientBuilder);
        Mockito.when(clientBuilder.additionalInterceptors(Mockito.any(ClientHttpRequestInterceptor.class))).thenReturn(clientBuilder);

        RestTemplate webClient = getWebClient("request_token.json", new TypeReference<OAuth2AccessToken>() {
        });
        Mockito.when(clientBuilder.build()).thenReturn(webClient);

        ApiClientService apiClientService = new ApiClientService(ssoService, clientBuilder, config);

        apiClientService
                .execute(new NotifySsoCommand(new HashMap<>()))
                .as(StepVerifier::create)
                .expectNextMatches(ssoResult -> "sdk-test".equals(ssoResult.getName()))
                .verifyComplete();

        apiClientService.execute(new GetApiClient());

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
    private RestTemplate getWebClient(String responseResourcePath, TypeReference type) {
        RestTemplate webClient = Mockito.mock(RestTemplate.class);
//        WebClient.RequestHeadersUriSpec headersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
//        WebClient.ResponseSpec resp = Mockito.mock(WebClient.ResponseSpec.class);
//        Mockito.when(webClient.get()).thenReturn(headersUriSpec);
//        Mockito.when(headersUriSpec.uri(Mockito.any(Function.class))).thenReturn(headersUriSpec);
//        Mockito.when(headersUriSpec.uri(Mockito.anyString())).thenReturn(headersUriSpec);
//        Mockito.when(headersUriSpec.headers(Mockito.any(Consumer.class))).thenReturn(headersUriSpec);
//        Mockito.when(headersUriSpec.retrieve()).thenReturn(resp);
//
//        WebClient.RequestBodyUriSpec bodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
//        Mockito.when(webClient.post()).thenReturn(bodyUriSpec);
//        Mockito.when(bodyUriSpec.uri(Mockito.any(Function.class))).thenReturn(bodyUriSpec);
//        Mockito.when(bodyUriSpec.uri(Mockito.anyString())).thenReturn(bodyUriSpec);
//        Mockito.when(bodyUriSpec.headers(Mockito.any(Consumer.class))).thenReturn(bodyUriSpec);
//        Mockito.when(bodyUriSpec.contentType(Mockito.any(MediaType.class))).thenReturn(bodyUriSpec);
//        Mockito.when(bodyUriSpec.body(Mockito.any())).thenReturn(headersUriSpec);
//
//        Mockito.when(resp.onStatus(Mockito.any(Predicate.class), Mockito.any(Function.class))).thenReturn(resp);
//
        ResponseMessage userDetail = ResponseMessage.ok(getResponseMessage("user_detail.json", new TypeReference<UserDetail>() {
        }));
        ResponseMessage userMenu = ResponseMessage.ok(getResponseMessage("user_menu.json", new TypeReference<List<MenuView>>() {
        }));
        ResponseMessage userAuth = ResponseMessage.ok(getResponseMessage("user_auth.json", new TypeReference<Map<String, Object>>() {
        }));
        Object token = getResponseMessage("request_token.json", new TypeReference<OAuth2AccessToken>() {
        });
        Mockito.when(webClient.exchange(Mockito.anyString(),
                                        Mockito.any(HttpMethod.class),
                                        Mockito.any(HttpEntity.class),
                                        Mockito.any(ParameterizedTypeReference.class)))
               .thenAnswer(invocation -> {
                   Object arg = invocation.getArguments()[0];
                   if (arg instanceof String) {
                       String url = (String) arg;
                       if (url.endsWith("/authorize/me")) {
                           return new ResponseEntity<>(userAuth, HttpStatus.OK);
                       }
                       if (url.equals("/user/detail")) {
                           return new ResponseEntity<>(userDetail, HttpStatus.OK);
                       }
                       if (url.endsWith("/menu/tree")) {
                           return new ResponseEntity<>(userMenu, HttpStatus.OK);
                       }
                   }
                   return null;
               });
        Mockito.when(webClient.exchange(Mockito.anyString(),
                                        Mockito.any(HttpMethod.class),
                                        Mockito.any(HttpEntity.class),
                                        Mockito.any(Class.class)))
               .thenAnswer(invocation -> {
                   Object arg = invocation.getArguments()[0];
                   if (arg instanceof String) {
                       String url = (String) arg;
                       if (url.endsWith("/menu/tree")) {
                           return new ResponseEntity(userMenu, HttpStatus.OK);
                       }
                       if (url.endsWith("/oauth2/token")) {
                           return new ResponseEntity(token, HttpStatus.OK);
                       }
                   }
                   return null;
               });
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
