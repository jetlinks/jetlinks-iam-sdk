package org.jetlinks.iam.core.command;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.iam.core.entity.SsoResult;

import java.util.Map;

/**
 * 处理单点登录回调.
 *
 * @author zhangji 2023/8/3
 */
@Getter
@Setter
public class NotifySsoCommand extends AbstractClientCommand<SsoResult> {

    private Map<String, String> parameter;

    public NotifySsoCommand(Map<String, String> parameter) {
        this.parameter = parameter;
    }
}
