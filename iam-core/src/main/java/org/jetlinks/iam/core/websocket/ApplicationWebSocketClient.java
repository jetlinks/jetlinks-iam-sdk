package org.jetlinks.iam.core.websocket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.OAuth2AccessToken;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * WebSocket客户端.
 *
 * @author zhangji 2023/8/18
 */
@Slf4j
@AllArgsConstructor
public class ApplicationWebSocketClient extends StandardWebSocketClient {

    private final RestTemplate restTemplate;
    private final ApiClientConfig apiClientConfig;
    private final WebSocketClient detect;


    public void execute(@Nonnull URI url, @Nonnull WebSocketHandler handler) throws Exception {
        execute(url, HttpHeaders.EMPTY, handler);
    }

    public void execute(@Nonnull URI url,
                        @Nonnull HttpHeaders headers,
                        @Nonnull WebSocketHandler handler) throws Exception {

        URI basePath = URI.create(apiClientConfig.getServerApiPath() + url);

        URI uri = UriComponentsBuilder
                .fromUri(basePath)
                .scheme("http".equalsIgnoreCase(basePath.getScheme()) ? "ws" : "wss")
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

        OAuth2AccessToken token = apiClientConfig.requestToken(restTemplate);
        executeWithToken(token, uri, httpHeaders, handler);
    }

    public void executeWithToken(OAuth2AccessToken token,
                                 @Nonnull URI url,
                                 @Nonnull HttpHeaders headers,
                                 @Nonnull WebSocketHandler handler) {
        headers.setBearerAuth(token.getAccessToken());

        WebSocketConnectionManager manager = new WebSocketConnectionManager(detect, handler, url.toString());
        manager.getHeaders().setBearerAuth(token.getAccessToken());
        manager.start();
    }

    public HttpHeaders installHttpHeaders(HttpHeaders httpHeaders) throws Exception {
        OAuth2AccessToken token = apiClientConfig.requestToken(restTemplate);
        httpHeaders.setBearerAuth(token.getAccessToken());
        return httpHeaders;
    }

}

