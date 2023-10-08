package org.jetlinks.iam.examples.spring.mvc.server.interceptor;

import lombok.AllArgsConstructor;
import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.token.AppUserTokenManager;
import org.jetlinks.iam.core.token.ParsedToken;
import org.jetlinks.iam.core.utils.TokenUtils;
import org.jetlinks.iam.sdk.service.UserService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/7
 */
@AllArgsConstructor
public class UserInterceptor implements HandlerInterceptor {

    private final AppUserTokenManager tokenManager;

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ParsedToken parsedToken = TokenUtils.parseTokenHeader(request);
        if (parsedToken == null) {
            throw new UnAuthorizedException();
        }

        if (!tokenManager.tokenIsLoggedIn(parsedToken.getToken())) {
            Authentication authentication = userService.getCurrentAuthentication(parsedToken);
            if (authentication == null) {
                throw new UnAuthorizedException();
            }
        }
        return true;
    }
}
