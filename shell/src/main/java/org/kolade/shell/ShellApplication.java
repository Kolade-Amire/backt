package org.kolade.shell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@ComponentScan(basePackages = {"org.kolade.service", "org.kolade.core", "org.kolade.mysql", "org.kolade.mongodb", "org.kolade.postgresql", "org.kolade.shell"})
public class ShellApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShellApplication.class, args);

    }

}
