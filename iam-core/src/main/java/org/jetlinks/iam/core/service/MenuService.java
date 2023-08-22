package org.jetlinks.iam.core.service;

import org.apache.commons.collections4.CollectionUtils;
import org.jetlinks.iam.core.configuration.MenuProperties;
import org.jetlinks.iam.core.entity.MenuEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务.
 *
 * @author zhangji 2023/8/18
 */
public class MenuService {

    private final MenuProperties menuProperties;

    private final List<MenuEntity> menuList = new ArrayList<>();

    public MenuService(MenuProperties menuProperties) {
        this.menuProperties = menuProperties;
    }

    public void add(List<MenuEntity> menu) {
        this.menuList.addAll(menu);
    }

    public List<MenuEntity> getAllMenu() {
        List<MenuEntity> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(menuProperties.getMenu())) {
            list.addAll(menuProperties.getMenu());
        }
        list.addAll(menuList);
        return list;
    }
}
