package org.jetlinks.iam.examples.webflux.server.service;

import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.jetlinks.iam.examples.webflux.server.entity.DemoDeviceEntity;
import org.springframework.stereotype.Service;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/8
 */
@Service
public class DemoDeviceService extends GenericReactiveCrudService<DemoDeviceEntity, String> {

}
