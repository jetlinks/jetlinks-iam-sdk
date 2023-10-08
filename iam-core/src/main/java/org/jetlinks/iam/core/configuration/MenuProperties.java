package org.jetlinks.iam.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.iam.core.entity.MenuEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 应用的菜单配置.
 *
 * @author zhangji 2023/8/11
 */
@ConfigurationProperties(prefix = "jetlinks.api.client")
@Getter
@Setter
public class MenuProperties {

    private List<MenuEntity> menu;

}
