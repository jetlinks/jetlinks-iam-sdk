package org.jetlinks.iam.core.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 默认权限ID编解码.
 * 集成到服务端时，添加后缀：“@ + appId”
 * 从服务端获取权限时，去除后缀
 *
 * @author zhangji 2023/8/18
 */
@AllArgsConstructor
public class DefaultPermissionCodec implements PermissionCodec {

    @Getter
    private final String appId;

    private final MenuService menuService;

    @Override
    public String encode(String permission) {
        return permission + "@" + getAppId();
    }

    @Override
    public String decode(String permission) {
        return menuService
                .getAllMenu()
                .stream()
                .map(menuEntity -> "@" + getAppId())
                .filter(permission::contains)
                .map(regex -> permission.split(regex)[0])
                .findFirst()
                .orElse(permission);
    }
}
