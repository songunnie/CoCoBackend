package com.igocst.coco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
public class CoCoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoCoApplication.class, args);
    }

//    @PostConstruct
//    void postConstruct() { TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul")); }
}
