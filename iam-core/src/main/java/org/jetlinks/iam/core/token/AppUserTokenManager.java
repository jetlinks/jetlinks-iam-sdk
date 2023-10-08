package org.jetlinks.iam.core.token;

import org.jetlinks.iam.core.entity.Authentication;

import java.util.Collection;
import java.util.List;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/14
 */
public interface AppUserTokenManager {

    /**
     * 根据token获取用户令牌信息
     *
     * @param token token
     * @return 令牌信息, 未授权时返回null
     */
    UserToken getByToken(String token);

    /**
     * 根据用户id，获取全部令牌信息，如果没有则返回空集合而不是<code>null</code>
     *
     * @param userId 用户id
     * @return 授权信息
     */
    List<UserToken> getByUserId(String userId);

    /**
     * @param userId 用户ID
     * @return 用户是否已经授权
     */
    Boolean userIsLoggedIn(String userId);

    /**
     * @param token token
     * @return token是否已登记
     */
    Boolean tokenIsLoggedIn(String token);

    /**
     * @return 总用户数量，一个用户多个地方登陆数量算1
     */
    Integer totalUser();

    /**
     * @return 总token数量
     */
    Integer totalToken();

    /**
     * @return 所有token
     */
    Collection<? extends UserToken> allLoggedUser();

    /**
     * 删除用户授权信息
     *
     * @param userId 用户ID
     */
    void signOutByUserId(String userId);

    /**
     * 根据token删除
     *
     * @param token 令牌
     * @see org.jetlinks.iam.core.token.event.UserTokenRemovedEvent
     */
    void signOutByToken(String token);

    /**
     * 修改userId的状态
     *
     * @param userId userId
     * @param state  状态
     * @see org.jetlinks.iam.core.token.event.UserTokenChangedEvent
     * @see AppUserTokenManager#changeTokenState
     */
    void changeUserState(String userId, TokenState state);

    /**
     * 修改token的状态
     *
     * @param token token
     * @param state 状态
     * @see org.jetlinks.iam.core.token.event.UserTokenChangedEvent
     */
    void changeTokenState(String token, TokenState state);

    /**
     * 登记一个用户的token
     *
     * @param token               token
     * @param userId              用户id
     * @param maxInactiveInterval 最大不活动时间(单位毫秒),超过后令牌状态{@link UserToken#getState()}将变为过期{@link TokenState#expired}
     * @see org.jetlinks.iam.core.token.event.UserTokenCreatedEvent
     */
    UserToken signIn(String token, String userId, long maxInactiveInterval);

    /**
     * 登记一个包含认证信息的token
     *
     * @param token               token
     * @param userId              用户ID
     * @param maxInactiveInterval 最大不活动时间(单位毫秒),小于0永不过期,超过后令牌状态{@link UserToken#getState()}将变为过期{@link TokenState#expired}
     * @param authentication      认证信息
     * @return token信息
     */
    default AuthenticationUserToken signIn(String token,
                                           String userId,
                                           long maxInactiveInterval,
                                           Authentication authentication) {
        throw new UnsupportedOperationException();
    }

    /**
     * 更新token,使其不过期
     *
     * @param token token
     */
    void touch(String token);

    /**
     * 检查已过期的token,并将其remove
     *
     * @see AppUserTokenManager#signOutByToken(String)
     */
    void checkExpiredToken();

}
