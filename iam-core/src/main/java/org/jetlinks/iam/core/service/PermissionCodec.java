package org.jetlinks.iam.core.service;

import reactor.core.publisher.Mono;

/**
 * 权限ID编解码.
 *
 * @author zhangji 2023/8/18
 */
public interface PermissionCodec {

    String encode(String permission, String owner);

    String decode(String permission);

}
