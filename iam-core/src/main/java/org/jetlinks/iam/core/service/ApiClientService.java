package org.jetlinks.iam.core.service;

import org.jetlinks.iam.core.command.Command;
import org.jetlinks.iam.core.command.GetApiClient;
import org.jetlinks.iam.core.command.GetWebsocketClient;
import org.jetlinks.iam.core.command.NotifySsoCommand;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.SsoResult;
import org.jetlinks.iam.core.utils.ValidatorUtils;
import org.jetlinks.iam.core.websocket.ApplicationWebSocketClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 应用服务.
 *
 * @author zhangji 2023/8/2
 */
public class ApiClientService {

    private final RestTemplateBuilder clientBuilder;

    private final ApiClientSsoService apiClientSsoService;

    private RestTemplate client;

    private Mono<ApplicationWebSocketClient> socketClientMono;

    private final ApiClientConfig config;

    public ApiClientService(ApiClientSsoService apiClientSsoService,
                            RestTemplateBuilder clientBuilder,
                            ApiClientConfig config) {
        this.apiClientSsoService = apiClientSsoService;
        this.clientBuilder = clientBuilder;
        this.config = config;

        ValidatorUtils.validate(config);
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
        throw new RuntimeException("error.application.command.unsupported");
    }

    /**
     * @return api客户端
     */
    private RestTemplate getApiClient() {
        return client == null ? client = createApiClient(config) : client;
    }

    protected RestTemplate createApiClient(ApiClientConfig config) {
        return config.createWebClient(clientBuilder);
    }

    /**
     * @return WebSocket客户端
     */
    private Mono<ApplicationWebSocketClient> getWebSocketClient() {
        return socketClientMono == null ? socketClientMono = createWebSocketClient(config) : socketClientMono;
    }

    protected Mono<ApplicationWebSocketClient> createWebSocketClient(ApiClientConfig config) {
        return Mono.just(new ApplicationWebSocketClient(
                clientBuilder.build(),
                config,
                new StandardWebSocketClient()
        ));
    }

    /**
     * 处理单点登录回调
     *
     * @param command 命令
     * @return 回调结果
     */
    private Mono<SsoResult> handleSsoNotify(NotifySsoCommand command) {
        return apiClientSsoService.handleSsoNotify(command, this.execute(new GetApiClient()));
    }
}
