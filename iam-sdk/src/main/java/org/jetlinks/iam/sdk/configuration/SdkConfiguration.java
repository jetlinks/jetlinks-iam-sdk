package org.jetlinks.iam.sdk.configuration;

import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.service.*;
import org.jetlinks.iam.sdk.service.SsoService;
import org.jetlinks.iam.sdk.service.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * sdk配置.
 *
 * @author zhangji 2023/8/3
 */
@AutoConfiguration
public class SdkConfiguration {

    @Bean
    public SsoService authorizationService(ApiClientService apiClientService,
                                           ApiClientConfig config,
                                           ObjectProvider<SsoHandler> objectProvider) {
        return new SsoService(apiClientService, config, objectProvider);
    }

    @Bean
    public UserService userService(ApiClientConfig config,
                                   UserRequestSender sender,
                                   ApiClientSsoService apiClientSsoService,
                                   ApiClientService apiClientService,
                                   PermissionCodec permissionCodec) {
        return new UserService(config, sender, apiClientSsoService, apiClientService, permissionCodec);
    }

}
