package org.jetlinks.iam.core.websocket;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * WebSocket订阅请求对象.
 *
 * @author zhangji 2023/8/18
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class SubscribeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private Type type;

    private String topic;

    private Map<String, Object> parameter;

    @Generated
    public enum Type {
        pub, sub, unsub, ping
    }

}

