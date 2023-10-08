package org.jetlinks.iam.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * 菜单资产权限配置定义.
 *
 * @author zhangji 2023/8/11
 */
@AllArgsConstructor
public enum AccessSupportState {
    //支持
    support("支持"),
    //不支持
    unsupported("不支持"),
    //间接的
    indirect("间接支持");

    private final String text;

    public String getText() {
        return text;
    }

    @JsonValue
    public String getValue() {
        return name();
    }
}
