package org.jetlinks.iam.core.websocket;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.OAuth2AccessToken;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * WebSocket客户端.
 *
 * @author zhangji 2023/8/18
 */
@AllArgsConstructor
public class ApplicationWebSocketClient implements WebSocketClient {

    private final WebClient tokenClient;
    private final ApiClientConfig apiClientConfig;
    private final WebSocketClient detect;

    @Override
    @Nonnull
    public Mono<Void> execute(@Nonnull URI url, @Nonnull WebSocketHandler handler) {

        return execute(url, HttpHeaders.EMPTY, handler);
    }

    @Override
    @Nonnull
    public Mono<Void> execute(@Nonnull URI url, @Nonnull HttpHeaders headers, @Nonnull WebSocketHandler handler) {

        URI basePath = URI.create(apiClientConfig.getServerApiPath() + url);

        URI uri = UriComponentsBuilder
                .fromUri(basePath)
                .scheme(basePath.getScheme())
                .build()
                .toUri();

        HttpHeaders httpHeaders = HttpHeaders.writableHttpHeaders(headers);

        if (CollectionUtils.isNotEmpty(apiClientConfig.getHeaders())) {
            apiClientConfig
                    .getHeaders()
                    .forEach(header -> httpHeaders.set(header.getKey(), header.getValue()));
        }

        if (CollectionUtils.isNotEmpty(apiClientConfig.getParameters())) {
            apiClientConfig
                    .getParameters()
                    .forEach(header -> httpHeaders.set(header.getKey(), header.getValue()));
        }

        return apiClientConfig
                .requestToken(tokenClient)
                .flatMap(token -> executeWithToken(token, uri, httpHeaders, handler));
    }

    public Mono<Void> executeWithToken(OAuth2AccessToken token, @Nonnull URI url, @Nonnull HttpHeaders headers, @Nonnull WebSocketHandler handler) {
        headers.setBearerAuth(token.getAccessToken());

        return detect.execute(url, headers, handler);
    }

    public Mono<HttpHeaders> installHttpHeaders(HttpHeaders httpHeaders) {
        return apiClientConfig
                .requestToken(tokenClient)
                .map(token -> {
                    httpHeaders.setBearerAuth(token.getAccessToken());
                    return httpHeaders;
                });
    }


}

