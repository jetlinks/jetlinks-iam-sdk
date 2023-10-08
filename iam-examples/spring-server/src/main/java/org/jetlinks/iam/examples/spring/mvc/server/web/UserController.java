package org.jetlinks.iam.examples.spring.mvc.server.web;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.entity.UserDetail;
import org.jetlinks.iam.core.token.AppUserTokenManager;
import org.jetlinks.iam.core.token.AuthenticationUserToken;
import org.jetlinks.iam.core.token.ParsedToken;
import org.jetlinks.iam.core.token.UserToken;
import org.jetlinks.iam.core.utils.TokenUtils;
import org.jetlinks.iam.examples.spring.mvc.server.interceptor.UnAuthorizedException;
import org.jetlinks.iam.sdk.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/4
 */
@RestController
@AllArgsConstructor
@Tag(name = "用户信息接口")
public class UserController {

    private final UserService userService;

    private final AppUserTokenManager tokenManager;

    @GetMapping("/user/detail")
    @Operation(summary = "获取当前用户信息")
    public UserDetail getUser(HttpServletRequest request) {
        ParsedToken token = TokenUtils.parseTokenHeader(request);
        if (token == null) {
            throw new UnAuthorizedException();
        }
        return userService.getCurrentUserDetail(token);
    }

    @GetMapping("/user/menu")
    @Operation(summary = "获取当前用户菜单")
    public List<MenuView> getUserMenu(@NotNull HttpServletRequest request) {
        ParsedToken token = TokenUtils.parseTokenHeader(request);
        if (token == null) {
            throw new UnAuthorizedException();
        }
        return userService.getCurrentMenu(token);
    }

    @GetMapping("/authorize/me")
    @Operation(summary = "获取当前用户权限")
    public Authentication getUserAuth(HttpServletRequest request) {
        ParsedToken token = TokenUtils.parseTokenHeader(request);
        if (token != null) {
            UserToken userToken = tokenManager.getByToken(token.getToken());
            if (userToken != null && !userToken.isExpired() && userToken instanceof AuthenticationUserToken) {
                return ((AuthenticationUserToken) userToken).getAuthentication();
            }
        }

        Authentication authentication = userService.getCurrentAuthentication(TokenUtils.parseTokenHeader(request));
        return JSONObject.parseObject(JSONObject.toJSONString(authentication), Authentication.class);
    }

}
