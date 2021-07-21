package com.main.farforlow.controller;

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
import com.main.farforlow.service.Messenger;
import com.main.farforlow.service.UserRequestsProcessor;
import com.main.farforlow.service.Utils;
import com.main.farforlow.view.ServiceMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PreAuthorize("@accessCheck.check(#token)")
@RestController
public class RequestController {
    private RequestDAL requestDAL;
    private RequestsSummaryDAL requestsSummaryDAL;
    private Utils utils;
    private Messenger messenger;
    private UserRequestsProcessor userRequestsProcessor;

    @Autowired
    public RequestController(RequestDAL requestDAL, RequestsSummaryDAL requestsSummaryDAL, Utils utils,
                             Messenger messenger, UserRequestsProcessor userRequestsProcessor) {
        this.requestDAL = requestDAL;
        this.requestsSummaryDAL = requestsSummaryDAL;
        this.utils = utils;
        this.messenger = messenger;
        this.userRequestsProcessor = userRequestsProcessor;
    }

    @PreAuthorize("permitAll()")
    @PostMapping(value = "/requests")
    public void telegramMessagesHandler(@RequestBody TelegramMessage telegramMessage) {

        if (telegramMessage == null) {
            return;
        }

        if (telegramMessage.getMessage().getFrom().getBot() != null &&
                telegramMessage.getMessage().getFrom().getBot()) {
            return;
        }
        if (telegramMessage.getMessage().getChat().getType().equals("group") &&
                (telegramMessage.getMessage().getText() == null ||
                !telegramMessage.getMessage().getText().startsWith("@farforlow_bot"))) {
            return;
        }
        if (telegramMessage.getMessage().getChat().getType().equals("group") &&
                telegramMessage.getMessage().getText().startsWith("@farforlow_bot")) {
            telegramMessage.getMessage().setText(telegramMessage.getMessage().getText().replaceAll("@farforlow_bot", "").trim());
        }

        UserRequest userRequest = null;
        switch (telegramMessage.getMessage().getText().trim()) {
            case "/stop":
                if (delete(telegramMessage)) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.STOP.text);
                }
                break;
            case "/start":
                if (create(telegramMessage)) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.DEPARTURE_CITY.text);
                } else {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.REQUST_EXISTS.text);
                }
                break;
            case "/help":
                userRequest = help(telegramMessage);
                if (userRequest == null) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.NO_REQUSTS.text);
                } else {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), userRequest.toString());
                }
                break;
            default:
                userRequest = update(telegramMessage);
                if (userRequest == null) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(),
                            "Something went wrong. Let's try again: send /stop and then /start.");
                }
                break;
        }
    }

    private Boolean delete(TelegramMessage telegramMessage) {
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

    private Boolean create(TelegramMessage telegramMessage) {
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

    private UserRequest help(TelegramMessage telegramMessage) {
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

    private UserRequest update(TelegramMessage telegramMessage) {
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
                searchPeriod = utils.getSearchPeriodDates(telegramMessage.getMessage().getText().trim(), userRequest.getMaxTripDurationDays());
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

    @GetMapping(value = "/requests")
    public ResponseEntity<List<UserRequest>> read(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getAll();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/{id}")
    public ResponseEntity<UserRequest> readOne(@RequestHeader(name = "token") String token, @PathVariable(name = "id") String id) {
        UserRequest userRequest = requestDAL.findById(id);
        return new ResponseEntity<>(userRequest, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/active")
    public ResponseEntity<List<UserRequest>> readActive(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getActive();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/incomplete")
    public ResponseEntity<List<UserRequest>> readIncomplete(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getIncomplete();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/queuing")
    public ResponseEntity<List<UserRequest>> readQueuing(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getQueuing();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/queuing/sorted")
    public ResponseEntity<List<UserRequest>> readQueuingSorted(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getQueuingSortedByCreationDate();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/deleted")
    public ResponseEntity<List<UserRequest>> readDeleted(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getDeleted();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @DeleteMapping(value = "/requests/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name = "token") String token, @PathVariable(name = "id") String id) {
        UserRequest userRequest = requestDAL.findById(id);
        if (userRequest == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            requestDAL.delete(userRequest);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping(value = "/requests/summary")
    public ResponseEntity<UserRequestsSummary> readSummary(@RequestHeader(name = "token") String token) {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        return new ResponseEntity<>(userRequestsSummary, HttpStatus.OK);
    }

    @PostMapping(value = "/requests/maxRequests/{maxRequests}")
    public ResponseEntity<UserRequestsSummary> updateMaxRequests(@RequestHeader(name = "token") String token,
                                                             @PathVariable(name = "maxRequests") String maxRequests) {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        if (userRequestsSummary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            try {
                int max = Integer.parseInt(maxRequests);
                userRequestsSummary.setMaxQuantityOfSearches(max);
                requestsSummaryDAL.updateOne(userRequestsSummary);
                return new ResponseEntity<>(userRequestsSummary, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            }
        }
    }

    @PostMapping(value = "/requests/currentRequests/{currentRequests}")
    public ResponseEntity<UserRequestsSummary> updateCurrentRequests(@RequestHeader(name = "token") String token,
                                                             @PathVariable(name = "currentRequests") String currentRequests) {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        if (userRequestsSummary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            try {
                int current = Integer.parseInt(currentRequests);
                userRequestsSummary.setCurrentQuantityOfSearches(current);
                requestsSummaryDAL.updateOne(userRequestsSummary);
                return new ResponseEntity<>(userRequestsSummary, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            }
        }
    }

    @GetMapping(value = "/requests/start")
    public void requestPerformanceStart(@RequestHeader(name = "token") String token) {
        userRequestsProcessor.requestsCalculation();
    }
}
