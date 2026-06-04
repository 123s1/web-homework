package com.museum.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MuseumReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuseumReservationApplication.class, args);
    }
}
