package org.jetlinks.iam.examples.spring.mvc.server.interceptor;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.token.AppUserTokenManager;
import org.jetlinks.iam.core.token.ParsedToken;
import org.jetlinks.iam.core.utils.TokenUtils;
import org.jetlinks.iam.sdk.service.UserService;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/7
 */
@AllArgsConstructor
public class DeviceInterceptor implements HandlerInterceptor {

    private final AppUserTokenManager tokenManager;

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ParsedToken parsedToken = TokenUtils.parseTokenHeader(request);
        if (parsedToken == null) {
            throw new UnAuthorizedException();
        }

        Authentication authentication = userService.getCurrentAuthentication(parsedToken);
        String uri = request.getRequestURI();
        if (uri.startsWith("/sdk/device")) {
            String method = request.getMethod();
            if (HttpMethod.POST.matches(method)) {
                Set<String> actions = new HashSet<>(1);
                actions.add("save");
                if (!checkPermission(authentication.getPermissions(), "device", actions)) {
                    throw new AccessDenyException("暂无访问权限：" + "device" + actions);
                }
            } else if (HttpMethod.GET.matches(method)) {
                Set<String> actions = new HashSet<>(1);
                actions.add("query");
                if (!checkPermission(authentication.getPermissions(), "device", actions)) {
                    throw new AccessDenyException("暂无访问权限：" + "device" + actions);
                }
            } else if (HttpMethod.DELETE.matches(method)) {
                Set<String> actions = new HashSet<>(1);
                actions.add("delete");
                if (!checkPermission(authentication.getPermissions(), "device", actions)) {
                    throw new AccessDenyException("暂无访问权限：" + "device" + actions);
                }
            }
        }

        return true;
    }

    private boolean checkPermission(List<Authentication.Permission> permissions, String permissionId, Set<String> actions) {
        if (!StringUtils.hasText(permissionId) || CollectionUtils.isEmpty(actions)) {
            return true;
        }
        for (Authentication.Permission permission : permissions) {
            if (permissionId.equals(permission.getId()) &&
                    permission.getActions() != null &&
                    permission.getActions().containsAll(actions)) {
                return true;
            }
        }
        return false;
    }
}
