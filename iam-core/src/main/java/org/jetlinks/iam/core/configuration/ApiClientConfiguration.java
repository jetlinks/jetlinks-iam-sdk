package org.jetlinks.iam.core.configuration;

import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenReactiveAuthenticationSupplier;
import org.jetlinks.iam.core.filter.ApiClientTokenFilter;
import org.jetlinks.iam.core.service.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 应用配置.
 *
 * @author zhangji 2023/8/4
 */
@AutoConfiguration
@EnableConfigurationProperties({ApiClientConfig.class, MenuProperties.class})
public class ApiClientConfiguration {

    @Bean
    public ApiClientSsoService apiClientSsoService(UserTokenManager userTokenManager,
                                                   UserRequestSender userService,
                                                   ApiClientConfig config,
                                                   UserTokenReactiveAuthenticationSupplier authenticationSupplier) {
        return new ApiClientSsoService(userTokenManager, userService, config, authenticationSupplier);
    }

    @Bean
    public ApiClientService apiClientService(ApiClientSsoService apiClientSsoService,
                                             WebClient.Builder clientBuilder,
                                             ApiClientConfig config) {
        return new ApiClientService(apiClientSsoService, clientBuilder, config);
    }

    @Bean
    public UserRequestSender userRequestSender() {
        return new UserRequestSender();
    }

    @Bean
    public ApiClientTokenFilter apiClientTokenFilter() {
        return new ApiClientTokenFilter();
    }

    @Bean
    public ApiClientAgent apiClientAgent(ApiClientConfig config,
                                         ApiClientSsoService ssoService,
                                         ApiClientService clientService) {
        return new ApiClientAgent(config, ssoService, clientService);
    }

    @Bean
    public MenuService menuService(MenuProperties menuProperties,
                                   ObjectProvider<MenuSupplier> providers) {
        MenuService menuService = new MenuService(menuProperties);
        providers.forEach(provider -> menuService.add(provider.get()));
        return menuService;
    }

}
