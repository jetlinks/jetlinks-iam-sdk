package org.jetlinks.iam.core.service;

import lombok.AllArgsConstructor;

/**
 * 默认权限ID编解码.
 * 集成到服务端时，添加后缀：“@ + owner”
 * 从服务端获取权限时，去除后缀
 *
 * @author zhangji 2023/8/18
 */
@AllArgsConstructor
public class DefaultPermissionCodec implements PermissionCodec {

    private final MenuService menuService;

    @Override
    public String encode(String permission, String owner) {
        return permission + "@" + owner;
    }

    @Override
    public String decode(String permission) {
        return menuService
                .getAllMenu()
                .stream()
                .map(menuEntity -> "@" + menuEntity.getOwner())
                .filter(permission::contains)
                .map(regex -> permission.split(regex)[0])
                .findFirst()
                .orElse(permission);
    }
}
