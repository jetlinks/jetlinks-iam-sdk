package org.jetlinks.iam.examples.spring.mvc.server.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.jetlinks.iam.examples.spring.mvc.server.entity.DemoDeviceEntity;
import org.jetlinks.iam.examples.spring.mvc.server.service.DemoDeviceService;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/8
 */
@RestController
@AllArgsConstructor
@Tag(name = "设备管理-示例接口")
@RequestMapping("/sdk/device")
public class SdkDemoDeviceController {

    private final DemoDeviceService service;

    @PostMapping
    public DemoDeviceEntity add(@RequestBody DemoDeviceEntity entity) {
        service.add(entity);
        return entity;
    }

    @GetMapping("/{id}")
    public DemoDeviceEntity find(@PathVariable String id) {
        DemoDeviceEntity entity = service.find(id);
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        return entity;
    }

    @DeleteMapping("/{id}")
    public DemoDeviceEntity delete(@PathVariable String id) {
        DemoDeviceEntity entity = service.find(id);
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        service.delete(id);
        return entity;
    }
}
