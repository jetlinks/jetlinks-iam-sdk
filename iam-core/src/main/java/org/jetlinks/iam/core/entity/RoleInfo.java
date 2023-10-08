package org.jetlinks.iam.core.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色信息.
 *
 * @author zhangji 2023/8/21
 */
@Getter
@Setter
public class RoleInfo {

    private String id;
    private String name;

    public RoleInfo with(Authentication.Dimension dimension) {
        RoleInfo info = this;
        info.setId(dimension.getId());
        info.setName(dimension.getName());
        return info;
    }

    public static RoleInfo of(Authentication.Dimension dimension) {
        return new RoleInfo().with(dimension);
    }
}