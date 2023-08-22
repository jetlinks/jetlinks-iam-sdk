package org.jetlinks.iam.core.filter;

import org.hswebframework.web.authorization.token.ParsedToken;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 应用的请求过滤器，透传token.
 *
 * @author zhangji 2023/8/4
 */
public class InternalTokenExchangeFilterFunction implements ExchangeFilterFunction {

    @Override
    @Nonnull
    public Mono<ClientResponse> filter(@Nonnull ClientRequest request, @Nonnull ExchangeFunction next) {

        return Mono
                .deferContextual(ctx -> {
                    ParsedToken token = ctx.<ParsedToken>getOrEmpty(ParsedToken.class).orElse(null);
                    if (token != null) {
                        return next
                                .exchange(
                                        ClientRequest
                                                .from(request)
                                                .headers(headers -> applyToken(token, headers))
                                                .build()
                                );
                    }
                    return next.exchange(request);
                });
    }

    public static void applyToken(ParsedToken token, HttpHeaders headers) {
        if ("default".equals(token.getType())) {
            headers.add("X-Access-Token", token.getToken());
        } else if ("basic".equals(token.getType())) {
            headers.setBasicAuth(token.getToken());
        } else {
            headers.setBearerAuth(token.getToken());
        }
    }

}
