package org.jetlinks.iam.sdk.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.MenuEntity;
import org.jetlinks.iam.core.service.MenuService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 菜单接口.
 *
 * @author zhangji 2023/8/11
 */
@RestController
@Tag(name = "菜单接口")
@RequestMapping("${jetlinks.api.client.config.menu-url:/api/menu}")
public class MenuController {

    private final ApiClientConfig config;

    private final MenuService menuService;

    public MenuController(ApiClientConfig config,
                          MenuService menuService) {
        this.config = config;
        this.menuService = menuService;
    }

    @PostMapping("/owner")
    @Operation(summary = "获取本系统ID")
    public Flux<String> getSystemMenuOwner() {
        return Flux.fromIterable(menuService.getAllMenu())
                   .mapNotNull(MenuEntity::getOwner)
                   .distinct();
    }

    @PostMapping("/owner/tree/{owner}")
    @Operation(summary = "获取本系统菜单信息（树结构）")
    public Flux<MenuEntity> getSystemMenuAsTree() {
        return Flux.fromIterable(menuService.getAllMenu())
                   .doOnNext(menu -> menu.setAppId(config.getClientId()))
                   .map(MenuEntity::init)
                   .collectList()
                   .flatMapIterable(list -> TreeSupportEntity.list2tree(list, MenuEntity::setChildren));
    }

}
