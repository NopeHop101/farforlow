package com.main.farforlow.service;

import com.main.farforlow.entity.Airport;
import com.main.farforlow.entity.Status;
import com.main.farforlow.entity.UserRequest;
import com.main.farforlow.entity.UserRequestsSummary;
import com.main.farforlow.entity.telegrammessage.TelegramMessage;
import com.main.farforlow.exception.CityException;
import com.main.farforlow.exception.DurationException;
import com.main.farforlow.exception.RequestsQuantityException;
import com.main.farforlow.exception.SearchPeriodException;
import com.main.farforlow.mongo.RequestDAL;
import com.main.farforlow.mongo.RequestsSummaryDAL;
import com.main.farforlow.view.ServiceMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TelegramCommandsProcessor {

    private RequestDAL requestDAL;
    private RequestsSummaryDAL requestsSummaryDAL;
    private Utils utils;
    private Messenger messenger;

    @Autowired
    public TelegramCommandsProcessor(RequestDAL requestDAL, RequestsSummaryDAL requestsSummaryDAL, Utils utils, Messenger messenger) {
        this.requestDAL = requestDAL;
        this.requestsSummaryDAL = requestsSummaryDAL;
        this.utils = utils;
        this.messenger = messenger;
    }

    public Boolean delete(TelegramMessage telegramMessage) {
        UserRequest userRequest = null;
        if (telegramMessage.getMessage().getChat().getType() != null &&
                telegramMessage.getMessage().getChat().getType().equals("group") &&
                telegramMessage.getMessage().getChat().getGroupId() != null) {
            userRequest = requestDAL.findByGroupIdAndNotDeleted(telegramMessage.getMessage().getChat().getGroupId());
        } else if (telegramMessage.getMessage().getFrom().getUserId() != null) {
            userRequest = requestDAL.findByUserIdAndNotDeleted(telegramMessage.getMessage().getFrom().getUserId());
        }
        if (userRequest == null) {
            return false;
        } else {
            if (userRequest.getStatus().equals(Status.ACTIVE)) {
                UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
                if (userRequestsSummary != null) {
                    userRequestsSummary.setCurrentQuantityOfSearches(userRequestsSummary.getCurrentQuantityOfSearches() - userRequest.getRequestsQuantity());
                    requestsSummaryDAL.updateOne(userRequestsSummary);
                }
            }
            userRequest.setStatus(Status.DELETED);
            requestDAL.update(userRequest);
            return true;
        }
    }

    public Boolean create(TelegramMessage telegramMessage) {
        UserRequest userRequest = null;
        if (telegramMessage.getMessage().getChat().getType() != null &&
                telegramMessage.getMessage().getChat().getType().equals("group") &&
                telegramMessage.getMessage().getChat().getGroupId() != null) {
            userRequest = requestDAL.findByGroupIdAndNotDeleted(telegramMessage.getMessage().getChat().getGroupId());
            if (userRequest != null) {
                return false;
            }
            userRequest = new UserRequest();
            userRequest.setTelegramGroupId(telegramMessage.getMessage().getChat().getGroupId());
            userRequest.setUserName(telegramMessage.getMessage().getChat().getTitle());
        } else if (telegramMessage.getMessage().getFrom().getUserId() != null) {
            userRequest = requestDAL.findByUserIdAndNotDeleted(telegramMessage.getMessage().getFrom().getUserId());
            if (userRequest != null) {
                return false;
            }
            userRequest = new UserRequest();
            userRequest.setTelegramUserId(telegramMessage.getMessage().getFrom().getUserId());
            userRequest.setUserName(telegramMessage.getMessage().getFrom().getUserName());
            userRequest.setFirstName(telegramMessage.getMessage().getFrom().getFirstName());
            userRequest.setLastName(telegramMessage.getMessage().getFrom().getLastName());
        } else {
            return false;
        }
        userRequest.setStatus(Status.INCOMPLETE);
        requestDAL.update(userRequest);
        return true;
    }

    public UserRequest help(TelegramMessage telegramMessage) {
        UserRequest userRequest = null;
        if (telegramMessage.getMessage().getChat().getType() != null &&
                telegramMessage.getMessage().getChat().getType().equals("group") &&
                telegramMessage.getMessage().getChat().getGroupId() != null) {
            userRequest = requestDAL.findByGroupIdAndNotDeleted(telegramMessage.getMessage().getChat().getGroupId());
        } else if (telegramMessage.getMessage().getFrom().getUserId() != null) {
            userRequest = requestDAL.findByUserIdAndNotDeleted(telegramMessage.getMessage().getFrom().getUserId());
        }
        return userRequest;
    }

    public UserRequest update(TelegramMessage telegramMessage) {
        UserRequest userRequest = null;
        if (telegramMessage.getMessage().getChat().getType() != null &&
                telegramMessage.getMessage().getChat().getType().equals("group") &&
                telegramMessage.getMessage().getChat().getGroupId() != null) {
            userRequest = requestDAL.findByGroupIdAndIncomplete(telegramMessage.getMessage().getChat().getGroupId());
        } else if (telegramMessage.getMessage().getFrom().getUserId() != null) {
            userRequest = requestDAL.findByUserIdAndIncomplete(telegramMessage.getMessage().getFrom().getUserId());
        }
        if (userRequest == null) {
            return null;
        }
        if (userRequest.getDepartureCity() == null) {
            String city = null;
            String country = null;
            if (telegramMessage.getMessage().getText() != null && telegramMessage.getMessage().getText().contains(",")) {
                city = telegramMessage.getMessage().getText().split(",")[0].trim();
                country = telegramMessage.getMessage().getText().split(",")[1].trim();
            } else {
                city = telegramMessage.getMessage().getText().trim();
            }
            List<Airport> airports = new ArrayList<>();
            try {
                airports = utils.getAirports(city, country);
            } catch (CityException e) {
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), e.getMessage());
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.DEPARTURE_CITY.text);
                return userRequest;
            }
            List<String> iataCodes = new ArrayList<>();
            for (Airport airport : airports) {
                iataCodes.add(airport.getCodeIata());
                city = airport.getCity();
                country = airport.getCountryCode();
            }
            userRequest.setDepartureCity(city);
            userRequest.setDepartureCountry(country);
            userRequest.setDepartureAirports(iataCodes);
            requestDAL.update(userRequest);
            messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.DESTINATION_CITY.text);
            return userRequest;
        } else if (userRequest.getDestinationCity() == null) {
            String city = null;
            String country = null;
            if (telegramMessage.getMessage().getText() != null && telegramMessage.getMessage().getText().contains(",")) {
                city = telegramMessage.getMessage().getText().split(",")[0].trim();
                country = telegramMessage.getMessage().getText().split(",")[1].trim();
            } else {
                city = telegramMessage.getMessage().getText().trim();
            }
            List<Airport> airports = new ArrayList<>();
            try {
                airports = utils.getAirports(city, country);
            } catch (CityException e) {
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), e.getMessage());
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.DESTINATION_CITY.text);
                return userRequest;
            }
            List<String> iataCodes = new ArrayList<>();
            for (Airport airport : airports) {
                iataCodes.add(airport.getCodeIata());
                city = airport.getCity();
                country = airport.getCountryCode();
            }
            userRequest.setDestinationCity(city);
            userRequest.setDestinationCountry(country);
            userRequest.setDestinationAirports(iataCodes);
            requestDAL.update(userRequest);
            messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.TRIP_DURATION.text);
            return userRequest;
        } else if (userRequest.getTripDuration() == null) {
            List<Integer> tripDuration = new ArrayList<>();
            try {
                tripDuration = utils.getDurationDays(telegramMessage.getMessage().getText().trim());
            } catch (DurationException e) {
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), e.getMessage());
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.TRIP_DURATION.text);
                return userRequest;
            }
            userRequest.setTripDuration(telegramMessage.getMessage().getText().trim());
            userRequest.setMinTripDurationDays(tripDuration.get(0));
            userRequest.setMaxTripDurationDays(tripDuration.get(1));
            requestDAL.update(userRequest);
            messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.SEARCH_PERIOD.text);
            return userRequest;
        } else if (userRequest.getSearchPeriod() == null) {
            List<Date> searchPeriod = new ArrayList<>();
            try {
                searchPeriod = utils.getSearchPeriodDates(telegramMessage.getMessage().getText().trim(), userRequest);
            } catch (SearchPeriodException e) {
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), e.getMessage());
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.SEARCH_PERIOD.text);
                return userRequest;
            }
            userRequest.setSearchPeriod(telegramMessage.getMessage().getText().trim());
            userRequest.setEarliestTripStartDate(searchPeriod.get(0));
            userRequest.setLatestReturnDate(searchPeriod.get(1));
            try {
                Integer optionsQuantity = utils.getRequestsQuantity(userRequest);
                userRequest.setRequestsQuantity(optionsQuantity);
                if (utils.isRequestInformationFull(userRequest)) {
                    userRequest.setCreatedDate(new Date());
                    userRequest.setStatus(Status.QUEUING);
                }
                requestDAL.update(userRequest);
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.REQUEST_FILLED.text);
                return userRequest;
            } catch (RequestsQuantityException e) {
                userRequest.setTripDuration(null);
                userRequest.setMinTripDurationDays(null);
                userRequest.setMaxTripDurationDays(null);
                userRequest.setSearchPeriod(null);
                userRequest.setEarliestTripStartDate(null);
                userRequest.setLatestReturnDate(null);
                requestDAL.update(userRequest);
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), e.getMessage());
                messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                        userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.TRIP_DURATION.text);
                return userRequest;
            }
        }
        return null;
    }
}
