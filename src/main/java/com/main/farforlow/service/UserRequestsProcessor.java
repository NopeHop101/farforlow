package com.main.farforlow.service;

import com.main.farforlow.entity.Result;
import com.main.farforlow.entity.Status;
import com.main.farforlow.entity.UserRequest;
import com.main.farforlow.entity.UserRequestsSummary;
import com.main.farforlow.mongo.RequestDAL;
import com.main.farforlow.mongo.RequestsSummaryDAL;
import com.main.farforlow.parser.ParserModel;
import com.main.farforlow.view.ServiceMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EnableScheduling
@Service
public class UserRequestsProcessor {

    private RequestDAL requestDAL;
    private RequestsSummaryDAL requestsSummaryDAL;
    private ParserModel parserModel;
    private Messenger messenger;

    @Autowired
    public UserRequestsProcessor(RequestDAL requestDAL, RequestsSummaryDAL requestsSummaryDAL, ParserModel parserModel, Messenger messenger) {
        this.requestDAL = requestDAL;
        this.requestsSummaryDAL = requestsSummaryDAL;
        this.parserModel = parserModel;
        this.messenger = messenger;
    }

    //Running requests daily starting at 2.01am
    @Scheduled(cron = "0 1 2 * * ?")
    public void requestsCalculation() {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        if (userRequestsSummary == null) {
            userRequestsSummary = new UserRequestsSummary();
            userRequestsSummary.setCurrentQuantityOfSearches(0);
            requestsSummaryDAL.createOne(userRequestsSummary);
        }
        List<UserRequest> activeRequests = requestDAL.getActive();
        if (activeRequests != null && !activeRequests.isEmpty()) {
            for (UserRequest userRequest : activeRequests) {
                if (userRequest.getEarliestTripStartDate().before(new Date())) {
                    userRequest.setStatus(Status.DELETED);
                    requestDAL.update(userRequest);
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.REQUST_EXPIRED.text);
                    userRequestsSummary.setCurrentQuantityOfSearches(userRequestsSummary.getCurrentQuantityOfSearches() -
                            userRequest.getRequestsQuantity());
                    requestsSummaryDAL.updateOne(userRequestsSummary);
                }
            }
        }
        List<UserRequest> queuingRequests = requestDAL.getQueuingSortedByCreationDate();
        if (queuingRequests != null && !queuingRequests.isEmpty()) {
            int queueCount = 0;
            for (UserRequest userRequest : queuingRequests) {
                if (userRequest.getEarliestTripStartDate().before(new Date())) {
                    userRequest.setStatus(Status.DELETED);
                    requestDAL.update(userRequest);
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.REQUST_EXPIRED.text);
                    continue;
                }
                if (userRequestsSummary.getMaxQuantityOfSearches() > userRequestsSummary.getCurrentQuantityOfSearches() +
                        userRequest.getRequestsQuantity()) {
                    userRequestsSummary.setCurrentQuantityOfSearches(userRequestsSummary.getCurrentQuantityOfSearches() +
                            userRequest.getRequestsQuantity());
                    userRequest.setStatus(Status.ACTIVE);
                    requestDAL.update(userRequest);
                    requestsSummaryDAL.updateOne(userRequestsSummary);
                } else {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.REQUST_QUEUING.text + queueCount);
                    queueCount++;
                }
            }
        }
        activeRequests = requestDAL.getActive();
        if (activeRequests != null && !activeRequests.isEmpty()) {
            for (UserRequest userRequest : activeRequests) {
                Result result = parserModel.requestExecutor(userRequest);
                if (result == null) {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.NOTHING_FOUND.text);
                    continue;
                }
                if (userRequest.getBestPrice() != null && userRequest.getBestPrice() != 0) {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(),
                            String.format("Best offer of the day: %d GBP (%+d vs lowest ever found price). More details: %s",
                                    result.getPriceGbp(), result.getPriceGbp() - userRequest.getBestPrice(), result.getLink()));
                } else {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(),
                            String.format("Best offer of the day: %d GBP. More details: %s",
                                    result.getPriceGbp(), result.getLink()));
                }
                if (userRequest.getBestPrice() == null || userRequest.getBestPrice() == 0 || result.getPriceGbp() < userRequest.getBestPrice()) {
                    userRequest.setBestPrice(result.getPriceGbp());
                }
                List<Result> results = userRequest.getResults();
                if (results == null) {
                    results = new ArrayList<>();
                }
                results.add(result);
                userRequest.setResults(results);
                requestDAL.update(userRequest);
            }
        }
        messenger.sendMessage("148678", "All users requests completed");
    }
}
