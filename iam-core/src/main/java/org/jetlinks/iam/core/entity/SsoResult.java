package org.jetlinks.iam.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 单点登录结果.
 *
 * @author zhangji 2023/8/3
 */
@Getter
@Setter
public class SsoResult implements Serializable {

    private static final long serialVersionUID = -6849794470754667710L;

    @Nonnull
    private String userId;

    private OAuth2AccessToken token;

    private String username;

    private String name;

    private String avatar;

    private String email;

    private String telephone;

    private String description;
    private Map<String, Object> others;

    public long getExpiresMillis() {
        return token == null ? 7200_000 : token.getExpiresIn() * 1000L;
    }

    public static SsoResult of(UserDetail userDetail) {
        SsoResult result = new SsoResult();
        result.setAvatar(userDetail.getAvatar());
        result.setName(userDetail.getName());
        result.setUserId(userDetail.getId());
        result.setUsername(userDetail.getUsername());
        result.setEmail(userDetail.getEmail());
        result.setTelephone(userDetail.getTelephone());
        result.setDescription(userDetail.getDescription());
        return result;
    }

    public SsoResult with(OAuth2AccessToken token) {
        this.token = token;
        return this;
    }

    public SsoResult withOther(String key, Object value) {
        if (others == null) {
            others = new HashMap<>();
        }
        others.put(key, value);
        return this;
    }
}
