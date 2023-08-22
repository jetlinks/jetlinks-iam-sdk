package org.jetlinks.iam.core.service;

import io.netty.buffer.ByteBufAllocator;
import org.hswebframework.web.authorization.Authentication;
import org.jetlinks.iam.core.command.GetWebsocketClient;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

/**
 * WebSocket客户端单元测试.
 *
 * @author zhangji 2023/8/21
 */
public class ApiClientAgentTest {

    @Test
    public void test() {
        ApiClientConfig config = new ApiClientConfig();

        ApiClientSsoService ssoService = Mockito.mock(ApiClientSsoService.class);
        Mockito
                .when(ssoService.signIn(Mockito.anyString(), Mockito.any(Authentication.class), Mockito.nullable(Long.class)))
                .thenReturn(Mono.empty());

        ApiClientService apiClientService = Mockito.mock(ApiClientService.class);
        WebSocketClient webSocketClient = Mockito.mock(WebSocketClient.class);
        Mockito
                .when(webSocketClient.execute(Mockito.any(URI.class), Mockito.any(WebSocketHandler.class)))
                .thenReturn(Mono.empty());
        Mockito.when(apiClientService.execute(Mockito.any(GetWebsocketClient.class)))
               .thenReturn(Mono.just(webSocketClient));


        ApiClientAgent agent = new ApiClientAgent(config, ssoService, apiClientService);

        agent.reconnect()
             .as(StepVerifier::create)
             .verifyComplete();

        ApiClientAgent.ProxyWebSocketHandler handler = new ApiClientAgent.ProxyWebSocketHandler();
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        Mockito.when(session.send(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(session.receive()).thenReturn(getWebSocketMessage());
        handler.handle(session)
               .as(StepVerifier::create)
               .verifyComplete();

    }

    private Flux<WebSocketMessage> getWebSocketMessage() {
        return DataBufferUtils
                .read(new ClassPathResource("websocket_response.json"),
                      new NettyDataBufferFactory(ByteBufAllocator.DEFAULT),
                      1024)
                .map(dataBuffer -> new WebSocketMessage(WebSocketMessage.Type.TEXT, dataBuffer));
    }

}
