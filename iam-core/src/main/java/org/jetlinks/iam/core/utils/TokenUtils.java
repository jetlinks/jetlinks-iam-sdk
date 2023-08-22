package org.jetlinks.iam.core.utils;

import org.hswebframework.web.authorization.token.ParsedToken;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * token工具类.
 *
 * @author zhangji 2023/8/10
 */
public class TokenUtils {
    public final static String TOKEN_HEADER = "X-Access-Token";
    public final static String TOKEN_ATTRIBUTE = ":X_Access_Token";

    public static ParsedToken parseTokenHeader(ServerHttpRequest request) {
        String token = Optional
                .ofNullable(request.getHeaders().getFirst(TOKEN_HEADER))
                .orElseGet(() -> request.getQueryParams().getFirst(TOKEN_ATTRIBUTE));

        if (token != null) {
            return ParsedToken.of(TOKEN_HEADER, token);
        }
        return null;
    }

    public static Mono<ParsedToken> parseTokenHeader(ClientRequest request) {
        String token = Optional
                .ofNullable(request.headers().getFirst(TOKEN_HEADER))
                .orElseGet(() -> request.attribute(TOKEN_ATTRIBUTE).map(Object::toString).orElse(null));

        if (token != null) {
            return Mono.just(ParsedToken.of(TOKEN_HEADER, token));
        }
        return Mono.empty();
    }
}
