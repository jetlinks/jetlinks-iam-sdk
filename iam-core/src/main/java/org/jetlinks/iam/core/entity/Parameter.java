package org.jetlinks.iam.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 请求参数定义.
 *
 * @author zhangji 2023/8/1
 */
@Getter
@Setter
public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;

    private String key;

    private String value;

}