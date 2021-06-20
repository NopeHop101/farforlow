package com.main.farforlow.digester.model;

import com.main.farforlow.entity.Airport;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.List;

@Service
public class AirportParserModel {

    private final String fileName = "src/main/java/com/main/farforlow/digester/datasource/airports.csv";

    public List<Airport> getAirports() {
        try {
            List<Airport> airports = new CsvToBeanBuilder(new FileReader(fileName))
                    .withType(Airport.class)
                    .build()
                    .parse();
            return airports;
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        AirportParserModel model = new AirportParserModel();
        List<Airport> airports = model.getAirports();
        int i = 0;
        for (Airport airport : airports) {
            System.out.println(airport);
            if (i == 10) {
                break;
            }
            i++;
        }
    }
}
