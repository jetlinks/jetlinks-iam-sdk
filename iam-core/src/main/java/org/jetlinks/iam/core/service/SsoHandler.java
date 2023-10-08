package org.jetlinks.iam.core.service;

import org.jetlinks.iam.core.entity.SsoResult;

/**
 * 单点登录回调-自定义处理.
 * 可实现此接口，注册到spring容器
 *
 * @author zhangji 2023/8/3
 */
public interface SsoHandler {

    /**
     * 处理结果
     *
     * @param ssoResult 单点登录结果
     */
    void handleResult(SsoResult ssoResult);

}
