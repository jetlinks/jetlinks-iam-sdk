package org.jetlinks.iam.sdk.service;


import org.jetlinks.iam.core.command.GetApiClient;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.entity.MenuView;
import org.jetlinks.iam.core.entity.UserDetail;
import org.jetlinks.iam.core.request.AuthenticationRequest;
import org.jetlinks.iam.core.request.UserDetailRequest;
import org.jetlinks.iam.core.request.UserMenuRequest;
import org.jetlinks.iam.core.service.ApiClientService;
import org.jetlinks.iam.core.service.ApiClientSsoService;
import org.jetlinks.iam.core.service.UserRequestSender;
import org.jetlinks.iam.core.token.ParsedToken;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 用户服务.
 *
 * @author zhangji 2023/8/10
 */
public class UserService {

    private final ApiClientConfig config;

    private final UserRequestSender sender;

    private final ApiClientSsoService apiClientSsoService;

    private final RestTemplate client;

    public UserService(ApiClientConfig config,
                       UserRequestSender sender,
                       ApiClientSsoService apiClientSsoService,
                       ApiClientService apiClientService) {
        this.config = config;
        this.sender = sender;
        this.apiClientSsoService = apiClientSsoService;
        this.client = apiClientService.execute(new GetApiClient());
    }

    /**
     * 查询当前用户信息
     *
     * @param parsedToken token
     * @return 用户信息
     */
    public UserDetail getCurrentUserDetail(ParsedToken parsedToken) {
        return sender.execute(new UserDetailRequest(parsedToken.getToken(), client));
    }

    /**
     * 查询当前用户菜单
     *
     * @param parsedToken token
     * @return 菜单
     */
    public List<MenuView> getCurrentMenu(ParsedToken parsedToken) {
        return sender.execute(new UserMenuRequest(config.getClientId(), parsedToken.getToken(), client));
    }

    /**
     * 查询当前用户权限
     *
     * @param parsedToken token
     * @return 权限
     */
    public Authentication getCurrentAuthentication(ParsedToken parsedToken) {
        Authentication authentication = sender
                .execute(new AuthenticationRequest(config.getClientId(), parsedToken.getToken(), client));
        if (authentication != null) {
            apiClientSsoService.signIn(parsedToken.getToken(), authentication, null);
        }
        return authentication;
    }
}
