package org.jetlinks.iam.examples.webflux.server.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.jetlinks.iam.examples.webflux.server.entity.DemoDeviceEntity;
import org.jetlinks.iam.examples.webflux.server.service.DemoDeviceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/8
 */
@RestController
@AllArgsConstructor
@Tag(name = "设备管理-示例接口")
@RequestMapping("/sdk/device")
public class SdkDemoDeviceController implements ReactiveServiceCrudController<DemoDeviceEntity, String> {

    private final DemoDeviceService service;

    @Override
    public ReactiveCrudService<DemoDeviceEntity, String> getService() {
        return service;
    }

}
