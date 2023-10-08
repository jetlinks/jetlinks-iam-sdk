package org.jetlinks.iam.examples.spring.mvc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 输入描述.
 *
 * @author zhangji 2023/8/3
 */
@ComponentScan(value = "org.jetlinks.iam.examples.spring.mvc.server")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
