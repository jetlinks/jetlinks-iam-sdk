package org.jetlinks.iam.core.service;

import org.jetlinks.iam.core.entity.MenuEntity;

import java.util.List;

/**
 * 菜单提供商.
 * 可实现此接口，注册到spring容器。可在菜单接口获取
 * @see MenuService#getAllMenu()
 *
 * @author zhangji 2023/8/11
 */
public interface MenuSupplier {

    List<MenuEntity> get();

}
