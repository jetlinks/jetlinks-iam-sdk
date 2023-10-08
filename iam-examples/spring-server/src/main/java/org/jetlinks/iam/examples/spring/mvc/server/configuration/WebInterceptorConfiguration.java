package org.jetlinks.iam.examples.spring.mvc.server.configuration;

import lombok.AllArgsConstructor;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.token.AppUserTokenManager;
import org.jetlinks.iam.examples.spring.mvc.server.interceptor.DeviceInterceptor;
import org.jetlinks.iam.examples.spring.mvc.server.interceptor.UserInterceptor;
import org.jetlinks.iam.sdk.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器配置.
 *
 * @author zhangji 2023/10/7
 */
@Configuration
@AllArgsConstructor
public class WebInterceptorConfiguration implements WebMvcConfigurer {

    private final AppUserTokenManager tokenManager;

    private final UserService userService;

    private final ApiClientConfig config;

    private final static List<String> EXCLUDES = new ArrayList<>();

    static {
        EXCLUDES.add("/");
        EXCLUDES.add("/js/**");
        EXCLUDES.add("/index.html");
        EXCLUDES.add("/user.html");
        EXCLUDES.add("/device.html");
        EXCLUDES.add("/token-set.html");
        EXCLUDES.add("/api/application/sso/**");
        EXCLUDES.add("/application/sso/**");
        EXCLUDES.add("/api/menu/**");
        EXCLUDES.add("/client/config/**");
        EXCLUDES.add("/error");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new UserInterceptor(tokenManager, userService))
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDES);


        registry
                .addInterceptor(new DeviceInterceptor(tokenManager, userService))
                .addPathPatterns("/sdk/device/**");

    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry
//                .addMapping("/**")
////                .allowedOrigins(URI.create(config.getServerApiPath()).getHost())
//                .allowedOrigins("**")
////                .allowCredentials(true)
//                .allowedMethods("POST", "GET", "DELETE", "PATCH");
//
//    }
}
