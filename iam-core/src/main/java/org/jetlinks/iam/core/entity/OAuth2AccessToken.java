package org.jetlinks.iam.core.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 单点登录token.
 *
 * @author zhangji 2023/8/3
 */
@Getter
@Setter
public class OAuth2AccessToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    @JsonProperty
    @JsonAlias("access_token")
    @JSONField(name = "access_token")
    private String accessToken;

    @JsonProperty
    @JsonAlias("refresh_token")
    @JSONField(name = "refresh_token")
    private String refreshToken;

    @JsonProperty
    @JsonAlias("expires_in")
    @JSONField(name = "expires_in")
    private int expiresIn;

    private long createTime = System.currentTimeMillis();

    public boolean isExpired() {
        return System.currentTimeMillis() - getCreateTime() >= (getExpiresIn() * 1000L);
    }

}
