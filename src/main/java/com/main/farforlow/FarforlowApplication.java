package com.main.farforlow;

import com.main.farforlow.digester.model.AirportParserModel;
import com.main.farforlow.elasticsearch.AirportSearchService;
import com.main.farforlow.service.UserRequestsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@SpringBootApplication
public class FarforlowApplication {

    public static void main(String[] args) {

        SpringApplication.run(FarforlowApplication.class, args);

        //creating database of airports in elastic search at initial launch
        AirportParserModel airportParserModel = new AirportParserModel();
        AirportSearchService airportSearchService = new AirportSearchService();
        airportSearchService.createAirportIndexBulk(airportParserModel.getAirports());

    }

}
