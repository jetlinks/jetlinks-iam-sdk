package org.jetlinks.iam.core.token;

import lombok.AllArgsConstructor;
import org.jetlinks.iam.core.entity.Authentication;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
@AllArgsConstructor
public class LocalAuthenticationUserToken extends LocalUserToken implements AuthenticationUserToken {

    private final Authentication authentication;

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }
}