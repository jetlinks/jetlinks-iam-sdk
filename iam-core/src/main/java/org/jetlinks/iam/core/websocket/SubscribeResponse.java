package org.jetlinks.iam.core.websocket;

import lombok.*;

/**
 * WebSocket响应消息.
 *
 * @author zhangji 2023/8/18
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Generated
public class SubscribeResponse {

    private String requestId;

    private String topic;

    private Object payload;

    private String type;

    private String message;

}
