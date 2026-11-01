package br.com.github.gpego1.routines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class RoutinesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutinesApplication.class, args);
    }

}
