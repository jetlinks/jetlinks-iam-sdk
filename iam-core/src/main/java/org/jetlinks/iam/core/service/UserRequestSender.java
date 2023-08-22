package org.jetlinks.iam.core.service;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.iam.core.request.ApiRequest;
import org.jetlinks.core.command.Command;
import org.jetlinks.core.command.CommandSupport;

import javax.annotation.Nonnull;


/**
 * 用户请求服务.
 *
 * @author zhangji 2023/8/4
 */
@Slf4j
public class UserRequestSender implements CommandSupport {

    @Override
    @SuppressWarnings("all")
    public <R> R execute(@Nonnull Command<R> command) {
        if (command instanceof ApiRequest) {
            return (R) ((ApiRequest<?>) command).execute();
        }
        throw new UnsupportedOperationException();
    }

}
