package org.jetlinks.iam.examples.spring.mvc.server.service;

import lombok.AllArgsConstructor;
import org.jetlinks.iam.examples.spring.mvc.server.dao.DemoDeviceDao;
import org.jetlinks.iam.examples.spring.mvc.server.entity.DemoDeviceEntity;
import org.springframework.stereotype.Service;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/8
 */
@Service
@AllArgsConstructor
public class DemoDeviceService {

    private final DemoDeviceDao dao;

    public void add(DemoDeviceEntity entity) {
        dao.save(entity);
    }

    public DemoDeviceEntity find(String id) {
        return dao.findById(id).orElse(null);
    }

    public void delete(String id) {
        dao.deleteById(id);
    }
}
