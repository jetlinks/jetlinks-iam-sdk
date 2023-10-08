package org.jetlinks.iam.sdk.web;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.entity.MenuEntity;
import org.jetlinks.iam.core.entity.ResponseMessage;
import org.jetlinks.iam.core.entity.TreeSupportEntity;
import org.jetlinks.iam.core.service.MenuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

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
    public String getSystemMenuOwner() {
        List<MenuEntity> menuList = menuService.getAllMenu();
        if (CollectionUtils.isNotEmpty(menuList)) {
            List<String> ownerList = menuList
                    .stream()
                    .map(MenuEntity::getOwner)
                    .distinct()
                    .collect(Collectors.toList());
            return JSONObject.toJSONString(ResponseMessage.ok(ownerList));
        }
        return JSONObject.toJSONString(ResponseMessage.ok());
    }

    @PostMapping("/owner/tree/{owner}")
    @Operation(summary = "获取本系统菜单信息（树结构）")
    public String getSystemMenuAsTree() {
        List<MenuEntity> menuList = menuService.getAllMenu();
        if (CollectionUtils.isNotEmpty(menuList)) {
            menuList.forEach(menu -> {
                menu.setAppId(config.getClientId());
                menu.init();
            });
            return JSONObject.toJSONString(ResponseMessage.ok(TreeSupportEntity.list2tree(menuList, MenuEntity::setChildren)));
        }
        return JSONObject.toJSONString(ResponseMessage.ok());
    }

}
