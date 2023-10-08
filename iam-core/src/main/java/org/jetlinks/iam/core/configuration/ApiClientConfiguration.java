package org.jetlinks.iam.core.configuration;

import org.jetlinks.iam.core.filter.ApiClientTokenFilter;
import org.jetlinks.iam.core.service.*;
import org.jetlinks.iam.core.token.AppUserTokenManager;
import org.jetlinks.iam.core.token.DefaultUserTokenManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

/**
 * 应用配置.
 *
 * @author zhangji 2023/8/4
 */
@AutoConfiguration
@EnableConfigurationProperties({ApiClientConfig.class, MenuProperties.class})
public class ApiClientConfiguration {

    @Bean
    @ConditionalOnMissingBean(AppUserTokenManager.class)
    public AppUserTokenManager appUserTokenManager() {
        return new DefaultUserTokenManager();
    }

    @Bean
    public ApiClientSsoService apiClientSsoService(AppUserTokenManager userTokenManager,
                                                   UserRequestSender userService,
                                                   ApiClientConfig config) {
        return new ApiClientSsoService(userTokenManager, userService, config);
    }

    @Bean
    public ApiClientService apiClientService(ApiClientSsoService apiClientSsoService,
                                             RestTemplateBuilder builder,
                                             ApiClientConfig config) {
        return new ApiClientService(apiClientSsoService, builder, config);
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
