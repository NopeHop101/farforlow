package com.main.farforlow.service;

import com.intuit.fuzzymatcher.component.MatchService;
import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Match;
import com.main.farforlow.entity.Airport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AirportSearchService {

    private HashMap<String, List<Airport>> airportByCityAndCountry;
    private HashMap<String, List<Airport>> airportByCity;
    private List<Document> citiesWithCounties;

    public void createAirportsDB(List<Airport> airports) {
        airportByCityAndCountry = new HashMap<>();
        airportByCity = new HashMap<>();
        citiesWithCounties = new ArrayList<>();

        Integer i = 0;
        for (Airport airport : airports) {
            if (airport.getCodeIata() != null && (("large_airport".equals(airport.getType()) && airport.getCodeIata().length() == 3) ||
            "SPU".equals(airport.getCodeIata()))) {
                if (airportByCityAndCountry.containsKey(airport.getCity().toLowerCase().trim() +
                        ", " + airport.getCountryCode().toLowerCase().trim())) {
                    List<Airport> currentAirports = airportByCityAndCountry.get(airport.getCity().toLowerCase().trim() +
                            ", " + airport.getCountryCode().toLowerCase().trim());
                    currentAirports.add(airport);
                    airportByCityAndCountry.put(airport.getCity().toLowerCase().trim() +
                            ", " + airport.getCountryCode().toLowerCase().trim(), currentAirports);
                } else {
                    Document.Builder doc = new Document.Builder(i.toString());
                    doc.addElement(new Element.Builder<String>().
                            setValue(airport.getCity().toLowerCase().trim()).
                            setType(ElementType.NAME).
                            setThreshold(0.2).
                            createElement());
                    doc.addElement(new Element.Builder<String>().
                            setValue(airport.getCountryCode().toLowerCase().trim()).
                            setType(ElementType.TEXT).
                            setThreshold(0.0).
                            createElement());
                    doc.setThreshold(0.2);
                    citiesWithCounties.add(doc.createDocument());
                    i++;
                    List<Airport> createAirports = new ArrayList<>();
                    createAirports.add(airport);
                    airportByCityAndCountry.put(airport.getCity().toLowerCase().trim() +
                            ", " + airport.getCountryCode().toLowerCase().trim(), createAirports);
                }

                if (airportByCity.containsKey(airport.getCity().toLowerCase().trim())) {
                    List<Airport> currentAirports = airportByCity.get(airport.getCity().toLowerCase().trim());
                    currentAirports.add(airport);
                    airportByCity.put(airport.getCity().toLowerCase().trim(), currentAirports);
                } else {
                    List<Airport> createAirports = new ArrayList<>();
                    createAirports.add(airport);
                    airportByCity.put(airport.getCity().toLowerCase().trim(), createAirports);
                }
            }
        }
    }

    public List<Airport> exactCityAndCountrySearch(String cityName, String country) {

        List<Airport> airports = new ArrayList<>();

        if (airportByCityAndCountry.containsKey(cityName.trim().toLowerCase() + ", " + country.trim().toLowerCase())) {
            airports = airportByCityAndCountry.get(cityName.trim().toLowerCase() + ", " + country.trim().toLowerCase());
        }

        return airports;
    }

    public List<Airport> exactCitySearch(String cityName) {

        List<Airport> airports = new ArrayList<>();
        if (airportByCity.containsKey(cityName.trim().toLowerCase())) {
            airports = airportByCity.get(cityName.trim().toLowerCase());
        }

        return airports;
    }

    public List<Airport> fuzzyCitySearch(String cityName) {

        List<Airport> airports = new ArrayList<>();

        MatchService matchService = new MatchService();

        Document.Builder doc = new Document.Builder("0");
        doc.addElement(new Element.Builder<String>().
                setValue(cityName.trim().toLowerCase()).
                setType(ElementType.NAME).
                setThreshold(0.2).
                createElement());
        doc.setThreshold(0.2);

        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(doc.createDocument(), citiesWithCounties);

        result.forEach((key, value) -> value.forEach(match -> {
            Document matchDoc = match.getMatchedWith();
            Set<Element> elements = matchDoc.getElements();
            String country = "";
            String city = "";
            for (Element<String> element : elements) {
                if (element.getValue().length() == 2) {
                    country = element.getValue();
                } else {
                    city = element.getValue();
                }
            }
            if (airportByCityAndCountry.containsKey(city + ", " + country)) {
                airports.addAll(airportByCityAndCountry.get(city + ", " + country));
            }
        }));

        return airports;
    }

    public static void main(String[] args) {
        AirportParserModel airportParserModel = new AirportParserModel();
        AirportSearchService airportSearchService = new AirportSearchService();
        airportSearchService.createAirportsDB(airportParserModel.getAirports());

        MatchService matchService = new MatchService();

        Document.Builder doc = new Document.Builder("0");
        doc.addElement(new Element.Builder<String>().
                setValue("mexico").
                setType(ElementType.NAME).
                setThreshold(0.2).
                createElement());
        doc.setThreshold(0.2);

        Map<String, List<Match<Document>>> result = matchService.applyMatchByDocId(doc.createDocument(), airportSearchService.citiesWithCounties);

        result.forEach((key, value) -> value.forEach(match -> {
            System.out.println("Data: " + match.getData() + " Matched With: " + match.getMatchedWith() + " Score: " + match.getScore().getResult());
        }));
    }

}
