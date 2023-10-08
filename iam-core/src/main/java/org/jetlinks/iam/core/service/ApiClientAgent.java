package org.jetlinks.iam.core.service;

import com.alibaba.fastjson.JSONObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.iam.core.command.GetWebsocketClient;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.websocket.SubscribeRequest;
import org.jetlinks.iam.core.websocket.SubscribeResponse;
import org.springframework.web.socket.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 应用客户端.
 * 通过WebSocket连接服务端，订阅用户授权变更事件
 *
 * @author zhangji 2023/8/18
 */
@Slf4j
public class ApiClientAgent {

    private final AtomicBoolean connecting = new AtomicBoolean();

    private ProxyWebSocketHandler handler;

    private final ApiClientConfig config;

    private final ApiClientSsoService ssoService;

    private final ApiClientService clientService;

    public ApiClientAgent(ApiClientConfig config,
                          ApiClientSsoService ssoService,
                          ApiClientService clientService) {
        this.config = config;
        this.ssoService = ssoService;
        this.clientService = clientService;

        Flux.interval(Duration.ofSeconds(30))
            .flatMap(ignore -> reconnect())
            .subscribe();
    }

    protected Mono<Void> reconnect() {
        if (handler != null) {
            return Mono.empty();
        }
        if (connecting.compareAndSet(false, true)) {
            return connectWebSocket()
                    .doFinally(s -> {
                        connecting.set(false);
                        handler = null;
                    })
                    .onErrorResume(err -> {
                        log.error(err.getMessage(), err);
                        return Mono.empty();
                    });
        }
        return Mono.empty();

    }

    protected Mono<Void> connectWebSocket() {
        handler = new ProxyWebSocketHandler();
        String id = "sdk-user-subscriber-" + config.getClientId();
        SubscribeRequest request = SubscribeRequest
                .builder()
                .id(id)
                .type(SubscribeRequest.Type.sub)
                .topic("/application/" + config.getClientId() + "/auth-changed")
                .build();
        handler.sender.emitNext(
                JSONObject.toJSONString(request),
                (signal, failure) -> failure == Sinks.EmitResult.FAIL_NON_SERIALIZED
        );

        return Flux
                .merge(
                        clientService
                                .execute(new GetWebsocketClient())
                                .flatMap(webSocketClient -> Mono
                                        .defer(() -> {
                                            try {
                                                webSocketClient.execute(URI.create("/messaging"), handler);
                                            } catch (Exception e) {
                                                return Mono.error(e);
                                            }
                                            return Mono.empty();
                                        })
                                        .doAfterTerminate(handler::dispose)),
                        handler.receiver.asFlux()
                )
                .cast(ProxyData.class)
                .filter(data -> data.getType().equals(ProxyDataType.data))
                .map(data -> JSONObject.parseObject(data.getData(), SubscribeResponse.class))
                .filter(response -> response.getRequestId().equals(id))
                .flatMap(response -> ssoService.signOut(response.getPayload().toString()))
                .then();
    }

    private static <T> Sinks.Many<T> createMany() {
        return Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

    static class ProxyWebSocketHandler implements WebSocketHandler, Disposable {

        private final Sinks.Many<ProxyData> receiver = createMany();
        private final Sinks.Many<String> sender = createMany();

        private Disposable disposable;

        private WebSocketSession session;

        @SneakyThrows
        private void closeSession() {
            if (this.session != null) {
                this.session.close();
            }
        }

        @Override
        public void dispose() {
            receiver.tryEmitComplete();
            sender.tryEmitComplete();
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            closeSession();
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String sessionId = UUID.randomUUID().toString();
            receiver.emitNext(new ProxyData(sessionId, ProxyDataType.connected, null),
                              (signal, failure) -> failure == Sinks.EmitResult.FAIL_NON_SERIALIZED);
            closeSession();
            this.session = session;

            disposable = sender
                    .asFlux()
                    .doOnNext(msg -> {
                        try {
                            session.sendMessage(new TextMessage(msg));
                        } catch (IOException e) {
                            log.error("api client send websocket message error", e);
                        }
                    })
                    .subscribe();
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            receiver
                    .emitNext(new ProxyData(
                                      session.getId(),
                                      ProxyDataType.data,
                                      (String) message.getPayload()
                              ),
                              (signal, failure) -> failure == Sinks.EmitResult.FAIL_NON_SERIALIZED);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            receiver.emitError(exception, (signal, failure) -> failure == Sinks.EmitResult.FAIL_NON_SERIALIZED);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            closeSession();
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }

    @Generated
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProxyData {
        private String sessionId;
        private ProxyDataType type;
        private String data;
    }

    public enum ProxyDataType {
        connected,
        data
    }
}
