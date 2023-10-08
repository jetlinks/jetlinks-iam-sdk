package org.jetlinks.iam.core.token;

import org.jetlinks.iam.core.entity.Authentication;
import org.jetlinks.iam.core.token.event.UserTokenChangedEvent;
import org.jetlinks.iam.core.token.event.UserTokenCreatedEvent;
import org.jetlinks.iam.core.token.event.UserTokenRemovedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
public class DefaultUserTokenManager implements AppUserTokenManager {

    protected final ConcurrentMap<String, LocalUserToken> tokenStorage;

    protected final ConcurrentMap<String, Set<String>> userStorage;

    public DefaultUserTokenManager() {
        this(new ConcurrentHashMap<>(256));

    }

    public DefaultUserTokenManager(ConcurrentMap<String, LocalUserToken> tokenStorage) {
        this(tokenStorage, new ConcurrentHashMap<>());
    }

    public DefaultUserTokenManager(ConcurrentMap<String, LocalUserToken> tokenStorage, ConcurrentMap<String, Set<String>> userStorage) {
        this.tokenStorage = tokenStorage;
        this.userStorage = userStorage;
    }

    //事件转发器
    private ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected Set<String> getUserToken(String userId) {
        return userStorage.computeIfAbsent(userId, key -> new HashSet<>());
    }

    private UserToken checkTimeout(UserToken detail) {
        if (null == detail) {
            return null;
        }
        if (detail.getMaxInactiveInterval() <= 0) {
            return detail;
        }
        if (System.currentTimeMillis() - detail.getLastRequestTime() > detail.getMaxInactiveInterval()) {
            changeTokenState(detail, TokenState.expired);
        }
        return detail;
    }

    @Override
    public UserToken getByToken(String token) {
        if (token == null) {
            return null;
        }

        return checkTimeout(tokenStorage.get(token));
    }

    @Override
    public List<UserToken> getByUserId(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Set<String> tokens = getUserToken(userId);
        if (tokens.isEmpty()) {
            userStorage.remove(userId);
            return Collections.emptyList();
        }
        return tokens
                .stream()
                .map(tokenStorage::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean userIsLoggedIn(String userId) {
        if (userId == null) {
            return false;
        }
        return getByUserId(userId)
                .stream()
                .filter(UserToken::isNormal)
                .findAny()
                .isPresent();
    }

    @Override
    public Boolean tokenIsLoggedIn(String token) {
        if (token == null) {
            return false;
        }
        UserToken userToken = getByToken(token);
        return userToken != null && !userToken.isExpired();
    }

    @Override
    public Integer totalUser() {
        return userStorage.size();
    }

    @Override
    public Integer totalToken() {
        return tokenStorage.size();
    }

    @Override
    public Collection<? extends UserToken> allLoggedUser() {
        return tokenStorage.values();
    }

    @Override
    public void signOutByUserId(String userId) {
        if (null == userId) {
            return;
        }

        Set<String> tokens = getUserToken(userId);
        tokens.forEach(token -> signOutByToken(token, false));
        tokens.clear();
        userStorage.remove(userId);
    }

    private void signOutByToken(String token, boolean removeUserToken) {
        if (token == null) {
            return;
        }
        LocalUserToken tokenObject = tokenStorage.remove(token);
        if (tokenObject != null) {
            String userId = tokenObject.getUserId();
            if (removeUserToken) {
                Set<String> tokens = getUserToken(userId);
                if (!tokens.isEmpty()) {
                    tokens.remove(token);
                }
                if (tokens.isEmpty()) {
                    userStorage.remove(tokenObject.getUserId());
                }
            }
            publishEvent(new UserTokenRemovedEvent(tokenObject));
        }
    }

    @Override
    public void signOutByToken(String token) {
        signOutByToken(token, true);
    }

    protected void publishEvent(ApplicationEvent event) {
        if (null != eventPublisher) {
            eventPublisher.publishEvent(event);
        }
    }

    public void changeTokenState(UserToken userToken, TokenState state) {
        if (null != userToken) {
            LocalUserToken token = ((LocalUserToken) userToken);
            LocalUserToken copy = token.copy();

            token.setState(state);
            syncToken(userToken);

            publishEvent(new UserTokenChangedEvent(copy, userToken));
        }
    }

    @Override
    public void changeTokenState(String token, TokenState state) {
        UserToken userToken = getByToken(token);
        if (userToken != null) {
            changeTokenState(userToken, state);
        }
    }

    @Override
    public void changeUserState(String user, TokenState state) {
        this
                .getByUserId(user)
                .forEach(token -> changeTokenState(token.getToken(), state));
    }

    @Override
    public UserToken signIn(String token, String userId, long maxInactiveInterval) {

        return doSignIn(token, userId, maxInactiveInterval, LocalUserToken::new);

    }

    private <T extends LocalUserToken> T doSignIn(String token,
                                                  String userId,
                                                  long maxInactiveInterval,
                                                  Supplier<T> tokenSupplier) {
        T detail = tokenSupplier.get();
        detail.setUserId(userId);
        detail.setToken(token);
        detail.setMaxInactiveInterval(maxInactiveInterval);
        detail.setState(TokenState.normal);
        Runnable doSign = () -> {
            tokenStorage.put(token, detail);

            getUserToken(userId).add(token);

            publishEvent(new UserTokenCreatedEvent(detail));
        };
        doSign.run();
        return detail;
    }

    @Override
    public AuthenticationUserToken signIn(String token, String userId, long maxInactiveInterval, Authentication authentication) {
        return doSignIn(token, userId, maxInactiveInterval, () -> new LocalAuthenticationUserToken(authentication));
    }

    @Override
    public void touch(String token) {
        LocalUserToken userToken = tokenStorage.get(token);
        if (null != userToken) {
            userToken.touch();
            syncToken(userToken);
        }
    }

    @Override
    public void checkExpiredToken() {
        tokenStorage
                .values()
                .forEach(token -> {
                    checkTimeout(token);
                    if (token.isExpired()) {
                        signOutByToken(token.getToken());
                    }
                });
    }

    /**
     * 同步令牌信息,如果使用redisson等来存储token，应该重写此方法并调用{@link this#tokenStorage}.put
     *
     * @param userToken 令牌
     */
    protected void syncToken(UserToken userToken) {
        //do noting
    }
}
