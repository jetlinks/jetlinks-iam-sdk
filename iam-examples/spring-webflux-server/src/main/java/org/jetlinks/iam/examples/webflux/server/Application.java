package org.jetlinks.iam.examples.webflux.server;

import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/3
 */
@EnableEasyormRepository("org.jetlinks.iam.examples.**.entity")
//@EnableAopAuthorize
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
