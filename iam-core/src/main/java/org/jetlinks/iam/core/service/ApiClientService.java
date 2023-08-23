package org.jetlinks.iam.core.service;

import org.hswebframework.web.exception.I18nSupportException;
import org.jetlinks.iam.core.command.Command;
import org.jetlinks.iam.core.command.GetApiClient;
import org.jetlinks.iam.core.command.GetWebsocketClient;
import org.jetlinks.iam.core.command.NotifySsoCommand;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.SsoResult;
import org.jetlinks.iam.core.websocket.ApplicationWebSocketClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 应用服务.
 *
 * @author zhangji 2023/8/2
 */
public class ApiClientService {

    private final WebClient.Builder clientBuilder;

    private final ApiClientSsoService apiClientSsoService;

    private Mono<WebClient> clientMono;

    private Mono<WebSocketClient> socketClientMono;

    private final ApiClientConfig config;

    public ApiClientService(ApiClientSsoService apiClientSsoService,
                            WebClient.Builder clientBuilder,
                            ApiClientConfig config) {
        this.apiClientSsoService = apiClientSsoService;
        this.clientBuilder = clientBuilder.clone();
        this.config = config;

        config.validate();
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <R> R execute(@Nonnull Command<R> command) {
        if (command instanceof NotifySsoCommand) {
            return (R) handleSsoNotify((NotifySsoCommand) command);
        }
        if (command instanceof GetApiClient) {
            return (R) getApiClient();
        }
        if (command instanceof GetWebsocketClient) {
            return (R) getWebSocketClient();
        }
        return requestUnknownCommand(command);
    }

    protected <R> R requestUnknownCommand(Command<R> command) {
        throw new I18nSupportException("error.application.command.unsupported");
    }

    /**
     * @return api客户端
     */
    private Mono<WebClient> getApiClient() {
        return clientMono == null ? clientMono = createApiClient(config) : clientMono;
    }

    protected Mono<WebClient> createApiClient(ApiClientConfig config) {
        return Mono.just(
                config.createWebClient(clientBuilder.clone())
        );
    }

    /**
     * @return WebSocket客户端
     */
    private Mono<WebSocketClient> getWebSocketClient() {
        return socketClientMono == null ? socketClientMono = createWebSocketClient(config) : socketClientMono;
    }

    protected Mono<WebSocketClient> createWebSocketClient(ApiClientConfig config) {
        return Mono.just(new ApplicationWebSocketClient(
                clientBuilder.build(),
                config,
                new ReactorNettyWebSocketClient()
        ));
    }

    /**
     * 处理单点登录回调
     *
     * @param command 命令
     * @return 回调结果
     */
    private Mono<SsoResult> handleSsoNotify(NotifySsoCommand command) {
        return this
                .execute(new GetApiClient())
                .flatMap(client -> apiClientSsoService.handleSsoNotify(command, client));
    }
}
