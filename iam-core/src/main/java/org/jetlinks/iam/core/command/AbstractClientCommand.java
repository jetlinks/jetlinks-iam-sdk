package org.jetlinks.iam.core.command;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

/**
 * 请求定义.
 *
 * @author zhangji 2023/8/2
 */
@Getter
@Setter
public abstract class AbstractClientCommand<T> implements Command<Mono<T>> {

}
