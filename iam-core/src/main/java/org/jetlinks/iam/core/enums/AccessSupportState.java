package org.jetlinks.iam.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.I18nEnumDict;

/**
 * 菜单资产权限配置定义.
 *
 * @author zhangji 2023/8/11
 */
@Getter
@AllArgsConstructor
public enum AccessSupportState implements I18nEnumDict<String> {
    //支持
    support("支持"),
    //不支持
    unsupported("不支持"),
    //间接的
    indirect("间接支持");

    private final String text;

    @Override
    public String getValue() {
        return name();
    }
}
