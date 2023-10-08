package org.jetlinks.iam.core.token;

import org.jetlinks.iam.core.entity.Authentication;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
public interface AuthenticationUserToken extends UserToken {

    /**
     * 获取认证信息
     *
     * @return auth
     * @see Authentication
     */
    Authentication getAuthentication();

}