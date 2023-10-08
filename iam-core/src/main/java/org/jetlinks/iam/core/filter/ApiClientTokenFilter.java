package org.jetlinks.iam.core.filter;

import org.jetlinks.iam.core.token.ParsedToken;
import org.jetlinks.iam.core.utils.TokenUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

/**
 * 过滤器，透传token.
 *
 * @author zhangji 2023/8/11
 */
public class ApiClientTokenFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.fromSupplier(() -> TokenUtils.parseTokenHeader(exchange.getRequest()))
                   .map(token -> chain
                           .filter(exchange)
                           .contextWrite(Context.of(ParsedToken.class, token)))
                   .defaultIfEmpty(chain.filter(exchange))
                   .flatMap(Function.identity());
    }

}
