package org.jetlinks.iam.examples.spring.mvc.server.dao;

import org.jetlinks.iam.examples.spring.mvc.server.entity.DemoDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/26
 */
public interface DemoDeviceDao extends JpaRepository<DemoDeviceEntity, String> {

}
