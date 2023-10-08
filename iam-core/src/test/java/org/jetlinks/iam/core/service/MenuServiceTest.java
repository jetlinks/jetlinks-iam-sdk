package org.jetlinks.iam.core.service;

import org.jetlinks.iam.core.configuration.MenuProperties;
import org.jetlinks.iam.core.entity.MenuEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/22
 */
public class MenuServiceTest {

    @Test
    public void test() {
        MenuProperties menuProperties = new MenuProperties();
        MenuEntity menu = new MenuEntity();
        menu.setId("test");
        menu.setName("test");
        menu.setOwner("sdk");
        menu.setUrl("/sdk/test");
        menuProperties.setMenu(Collections.singletonList(menu));

        MenuService menuService = new MenuService(menuProperties);
        TestMenuSupplier menuSupplier = new TestMenuSupplier();
        menuService.add(menuSupplier.get());

        List<MenuEntity> menuList = menuService.getAllMenu();
        Assertions.assertNotNull(menuList);
        Assertions.assertEquals(2, menuList.size());
    }

    static class TestMenuSupplier implements MenuSupplier {

        List<MenuEntity> menu = new ArrayList<>();

        public TestMenuSupplier() {
            MenuEntity menu = new MenuEntity();
            menu.setId("test-supplier");
            menu.setName("test-supplier");
            menu.setOwner("sdk");
            menu.setUrl("/sdk/test/supplier");
            this.menu.add(menu);
        }

        @Override
        public List<MenuEntity> get() {
            return menu;
        }
    }
}
