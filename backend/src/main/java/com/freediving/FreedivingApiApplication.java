package com.freediving;

import com.freediving.config.CosProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CosProperties.class)
public class FreedivingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreedivingApiApplication.class, args);
    }
}
