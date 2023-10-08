package org.jetlinks.iam.core.entity;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/25
 */
@FunctionalInterface
public interface IDGenerator<T> {
    T generate();
}
