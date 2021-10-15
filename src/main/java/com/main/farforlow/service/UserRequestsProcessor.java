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
import java.util.HashSet;
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

    //Running requests daily starting at 1.01am server time
    @Scheduled(cron = "0 1 1 * * ?")
    public void requestsCalculation() {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        if (userRequestsSummary == null) {
            userRequestsSummary = new UserRequestsSummary();
            userRequestsSummary.setCurrentQuantityOfSearches(0);
            requestsSummaryDAL.createOne(userRequestsSummary);
        }
        userRequestsSummary.setFailuresCount(0);
        userRequestsSummary.setSecondAttemptSuccessCount(0);
        userRequestsSummary.setFailedProxies(new HashSet<String>());
        requestsSummaryDAL.updateOne(userRequestsSummary);
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
                    userRequestsSummary.setCurrentQuantityOfSearches(userRequestsSummary.getCurrentQuantityOfSearches() -
                            userRequest.getRequestsQuantity());
                    requestsSummaryDAL.updateOne(userRequestsSummary);
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
                List<Result> results = parserModel.requestExecutor(userRequest);
                if (results == null && userRequest.getResults().size() >= 1 && userRequest.getResults().get(userRequest.getResults().size() - 1) == null) {
                    userRequest.setStatus(Status.DELETED);
                    requestDAL.update(userRequest);
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.CLOSE_REQUST.text);
                    userRequestsSummary.setCurrentQuantityOfSearches(userRequestsSummary.getCurrentQuantityOfSearches() -
                            userRequest.getRequestsQuantity());
                    requestsSummaryDAL.updateOne(userRequestsSummary);
                    continue;
                } else if (results == null) {
                    List<Result> previousResults = userRequest.getResults();
                    if (previousResults == null) {
                        previousResults = new ArrayList<>();
                    }
                    previousResults.add(null);
                    userRequest.setResults(previousResults);
                    requestDAL.update(userRequest);
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), ServiceMessages.NO_RESULTS.text);
                    continue;
                }

                Result result = results.get(0);
                if (userRequest.getBestPrice() != null && userRequest.getBestPrice() != 0) {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(),
                            String.format(ServiceMessages.DAILY_OFFER_VS_BEST.text,
                                    result.getPrice(), result.getCurrency(), result.getPrice() - userRequest.getBestPrice(), result.getLink()));
                } else {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(),
                            String.format(ServiceMessages.DAILY_OFFER.text,
                                    result.getPrice(), result.getCurrency(), result.getLink()));
                }
                if (userRequest.getBestPrice() == null || userRequest.getBestPrice() == 0 || result.getPrice() < userRequest.getBestPrice()) {
                    userRequest.setBestPrice(result.getPrice());
                }
                List<Result> previousResults = userRequest.getResults();
                if (previousResults == null) {
                    previousResults = new ArrayList<>();
                }
                previousResults.add(result);
                userRequest.setResults(previousResults);
                requestDAL.update(userRequest);

                if (results.size() > 1) {
                    StringBuilder message = new StringBuilder();
                    message.append(ServiceMessages.OTHER_OFFERS_HEADER.text);
                    int size = results.size() <= 3 ? results.size() : 4;
                    for (int i = 1; i < size; i++) {
                        message.append(String.format(ServiceMessages.OTHER_OFFER.text,
                                i, results.get(i).getPrice(), results.get(i).getCurrency(), results.get(i).getLink()));
                    }
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                                    userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(),
                            message.toString());
                }
            }
        }
        messenger.sendMessage(System.getenv("BOT_OWNER_TELEGRAM_ID"), requestsSummaryDAL.getOne().toString());
    }
}
