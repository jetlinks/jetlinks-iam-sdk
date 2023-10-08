package org.jetlinks.iam.core.service;

import org.jetlinks.iam.core.command.GetWebsocketClient;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.websocket.ApplicationWebSocketClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

/**
 * WebSocket客户端单元测试.
 *
 * @author zhangji 2023/8/21
 */
public class ApiClientAgentTest {

    @Test
    public void test() throws Exception {
        ApiClientConfig config = new ApiClientConfig();

        ApiClientSsoService ssoService = Mockito.mock(ApiClientSsoService.class);
        Mockito
                .when(ssoService.signIn(Mockito.anyString(), Mockito.any(Authentication.class), Mockito.nullable(Long.class)))
                .thenReturn(Mono.empty());

        ApiClientService apiClientService = Mockito.mock(ApiClientService.class);
        ApplicationWebSocketClient webSocketClient = Mockito.mock(ApplicationWebSocketClient.class);
        Mockito.when(apiClientService.execute(Mockito.any(GetWebsocketClient.class)))
               .thenReturn(Mono.just(webSocketClient));


        ApiClientAgent agent = new ApiClientAgent(config, ssoService, apiClientService);

        agent.reconnect()
             .as(StepVerifier::create)
             .verifyComplete();

        ApiClientAgent.ProxyWebSocketHandler handler = new ApiClientAgent.ProxyWebSocketHandler();
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        Mockito.when(session.getId()).thenReturn("test");
        handler.handleMessage(session, getWebSocketMessage());
    }

    private WebSocketMessage getWebSocketMessage() throws IOException {
        Resource resource = new ClassPathResource("websocket_response.json");
        return new TextMessage(StreamUtils.copyToByteArray(resource.getInputStream()));
    }

}
