package com.main.farforlow.service;

import com.main.farforlow.entity.Airport;
import com.main.farforlow.entity.UserRequest;
import com.main.farforlow.exception.CityException;
import com.main.farforlow.exception.DurationException;
import com.main.farforlow.exception.RequestsQuantityException;
import com.main.farforlow.exception.SearchPeriodException;
import com.main.farforlow.view.ServiceMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class Utils {

    private final Integer MAX_REQUESTS_NUMBER = Integer.parseInt(System.getenv("MAX_SEARCHES_PER_REQUEST"));
    private boolean createAirportsDatabase = true;

    @Autowired
    private AirportSearchService airportSearchService;

    @Autowired
    AirportParserModel airportParserModel;

    public List<Airport> getAirports(String cityName, String countryCode) throws CityException {

        if (createAirportsDatabase) {
            airportSearchService.createAirportsDB(airportParserModel.getAirports());
            createAirportsDatabase = false;
        }

        List<Airport> res = null;
        if (cityName != null && cityName.length() > 2 && countryCode != null && !countryCode.isEmpty()) {
            res = airportSearchService.exactCityAndCountrySearch(cityName, countryCode);
            if (res == null || res.isEmpty()) {
                throw new CityException(ServiceMessages.CITY_EXCEPTION_NO_OPTIONS.text);
            }
            Set<String> cityNames = new HashSet<>();
            for (Airport airport : res) {
                cityNames.add(airport.getCity());
            }
            if (cityNames.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (String city : cityNames) {
                    sb.append(" ").append(city);
                    sb.append(", ").append(countryCode).append(";");
                }
                String cities = sb.toString().trim();
                throw new CityException(String.format(ServiceMessages.CITY_EXCEPTION_WITH_OPTIONS.text, cities.substring(0, cities.length() - 1)));
            }
            return res;
        } else if (cityName != null && cityName.length() > 2) {
            res = airportSearchService.exactCitySearch(cityName);
            if (res == null || res.isEmpty()) {
                res = airportSearchService.fuzzyCitySearch(cityName);
                if (res == null || res.isEmpty()) {
                    throw new CityException(ServiceMessages.CITY_EXCEPTION_NO_OPTIONS.text);
                }
                Set<String> options = new HashSet<>();
                for (Airport airport : res) {
                    options.add(airport.getCity() + ", " + airport.getCountryCode());
                }
                if (options.size() > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (String option : options) {
                        sb.append(" ").append(option).append(";");
                    }
                    String cities = sb.toString().trim();
                    throw new CityException(String.format(ServiceMessages.CITY_EXCEPTION_WITH_OPTIONS.text, cities.substring(0, cities.length() - 1)));
                }
                return res;
            }
            Set<String> options = new HashSet<>();
            for (Airport airport : res) {
                options.add(airport.getCity() + ", " + airport.getCountryCode());
            }
            if (options.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (String option : options) {
                    sb.append(" ").append(option).append(";");
                }
                String cities = sb.toString().trim();
                throw new CityException(String.format(ServiceMessages.CITY_EXCEPTION_WITH_OPTIONS.text, cities.substring(0, cities.length() - 1)));
            }
            return res;
        }
        throw new CityException(ServiceMessages.CITY_EXCEPTION_NO_OPTIONS.text);
    }

    public List<Integer> getDurationDays(String tripDuration) throws DurationException {
        List<Integer> res = new ArrayList<>();
        if (tripDuration == null) {
            throw new DurationException();
        }
        String[] minMaxDurations = tripDuration.trim().split("-");
        if (minMaxDurations.length != 2) {
            throw new DurationException();
        }
        try {
            res.add(Integer.parseInt(minMaxDurations[0]));
            res.add(Integer.parseInt(minMaxDurations[1]));
        } catch (Exception e) {
            throw new DurationException();
        }
        if (res.get(0) > res.get(1)) {
            throw new DurationException();
        }
        return res;
    }

    public List<Date> getSearchPeriodDates(String searchPeriod, UserRequest userRequest) throws SearchPeriodException {
        List<Date> res = new ArrayList<>();
        if (searchPeriod == null || userRequest.getMinTripDurationDays() == null || userRequest.getMaxTripDurationDays() == null) {
            throw new SearchPeriodException();
        }
        String[] startEndDates = searchPeriod.trim().split("-");
        if (startEndDates.length != 2) {
            throw new SearchPeriodException();
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            res.add(formatter.parse(startEndDates[0]));
            res.add(formatter.parse(startEndDates[1]));
        } catch (Exception e) {
            throw new SearchPeriodException();
        }
        LocalDateTime startDate = res.get(0).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDate = res.get(1).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        long searchPeriodDays = Duration.between(startDate, endDate).toDays();
        if (res.get(0).before(new Date()) || res.get(0).after(res.get(1))) {
            throw new SearchPeriodException();
        }

        if (searchPeriodDays < userRequest.getMaxTripDurationDays()) {
            userRequest.setMaxTripDurationDays((int) searchPeriodDays);
        }
        if (userRequest.getMinTripDurationDays() > userRequest.getMaxTripDurationDays()) {
            if (userRequest.getMaxTripDurationDays() > 0) {
                userRequest.setMinTripDurationDays(userRequest.getMaxTripDurationDays() - 1);
            } else {
                userRequest.setMinTripDurationDays(0);
            }
        }
        userRequest.setTripDuration(userRequest.getMinTripDurationDays() + "-" + userRequest.getMaxTripDurationDays());

        return res;
    }

    public Integer getRequestsQuantity(UserRequest userRequest) throws RequestsQuantityException {
        int res;
        LocalDateTime startDate = userRequest.getEarliestTripStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDate = userRequest.getLatestReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        long searchPeriodDays = Duration.between(startDate, endDate).toDays();
        res = userRequest.getDepartureAirports().size() * userRequest.getDestinationAirports().size() *
                (userRequest.getMaxTripDurationDays() - userRequest.getMinTripDurationDays() + 1) *
                ((int) searchPeriodDays - (userRequest.getMaxTripDurationDays() + userRequest.getMinTripDurationDays()) / 2 + 1);
        if (res > MAX_REQUESTS_NUMBER) {
            throw new RequestsQuantityException(String.format(
                    ServiceMessages.REQUESTS_QUANTITIES_EXCEPTION.text,
                    res, MAX_REQUESTS_NUMBER));
        }
        return res;
    }

    public Boolean isRequestInformationFull(UserRequest userRequest) {
        return userRequest.getDepartureAirports() != null && !userRequest.getDepartureAirports().isEmpty() &&
                userRequest.getDestinationAirports() != null && !userRequest.getDestinationAirports().isEmpty() &&
                userRequest.getMinTripDurationDays() != null && userRequest.getMinTripDurationDays() > 0 &&
                userRequest.getMaxTripDurationDays() != null && userRequest.getMaxTripDurationDays() >= userRequest.getMinTripDurationDays() &&
                userRequest.getEarliestTripStartDate() != null && !userRequest.getEarliestTripStartDate().before(new Date()) &&
                userRequest.getLatestReturnDate() != null && !userRequest.getLatestReturnDate().before(userRequest.getEarliestTripStartDate()) &&
                userRequest.getRequestsQuantity() != null && userRequest.getRequestsQuantity() > 0;
    }
}
