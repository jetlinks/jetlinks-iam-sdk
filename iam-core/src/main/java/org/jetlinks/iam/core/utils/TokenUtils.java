package org.jetlinks.iam.core.utils;

import org.jetlinks.iam.core.token.ParsedToken;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
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

    public static ParsedToken parseTokenHeader(HttpServletRequest request) {
        String token = Optional
                .ofNullable(request.getHeader(TOKEN_HEADER))
                .orElseGet(() -> request.getParameter(TOKEN_ATTRIBUTE));

        if (token != null) {
            return ParsedToken.of(TOKEN_HEADER, token);
        }
        return null;
    }
}
