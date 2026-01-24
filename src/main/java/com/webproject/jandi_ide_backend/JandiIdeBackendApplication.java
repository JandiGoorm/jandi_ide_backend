package com.webproject.jandi_ide_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration;

@SpringBootApplication(exclude = {
    SystemMetricsAutoConfiguration.class,
    TomcatMetricsAutoConfiguration.class
})
public class JandiIdeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JandiIdeBackendApplication.class, args);
    }

}
