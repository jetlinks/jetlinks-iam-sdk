package org.jetlinks.iam.core.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.EntityFactoryHolder;
import org.hswebframework.web.authorization.Dimension;

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

    public static RoleInfo of() {
        return EntityFactoryHolder.newInstance(RoleInfo.class, RoleInfo::new);
    }

    public RoleInfo with(Dimension dimension) {
        RoleInfo info = this;
        info.setId(dimension.getId());
        info.setName(dimension.getName());
        return info;
    }

    public static RoleInfo of(Dimension dimension) {
        return of().with(dimension);
    }
}