package com.main.farforlow.parser;

import com.main.farforlow.entity.Result;
import com.main.farforlow.entity.UserRequest;
import com.main.farforlow.entity.UserRequestsSummary;
import com.main.farforlow.mongo.RequestsSummaryDAL;
import com.main.farforlow.mongo.RequestsSummaryDALImpl;
import com.main.farforlow.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class ParserModel {

    private final SkiplaggedClient skiplaggedClient;
    private final Utils utils;
    private RequestsSummaryDAL requestsSummaryDAL;

    private Set<String> failedProxies = new HashSet<>();
    private int failuresCount;
    private int secondAttemptSuccessCount;

    @Autowired
    public ParserModel(SkiplaggedClient skiplaggedClient, Utils utils, RequestsSummaryDAL requestsSummaryDAL) {
        this.skiplaggedClient = skiplaggedClient;
        this.utils = utils;
        this.requestsSummaryDAL = requestsSummaryDAL;
    }

    public List<Result> requestExecutor(UserRequest userRequest) {
        List<Result> res = new ArrayList<>();
        if (!utils.isRequestInformationFull(userRequest)) {
            return null;
        }
        List<Callable<Result>> tasks = new ArrayList<Callable<Result>>();
        for (String departureAirport : userRequest.getDepartureAirports()) {
            for (String destinationAirport : userRequest.getDestinationAirports()) {
                for (int duration = userRequest.getMinTripDurationDays(); duration <= userRequest.getMaxTripDurationDays(); duration++) {
                    for (LocalDateTime departureDate = userRequest.getEarliestTripStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                         !departureDate.plusDays(duration).isAfter(userRequest.getLatestReturnDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                         departureDate = departureDate.plusDays(1)) {

                        Date finalDepartureDate = Date.from(departureDate.atZone(ZoneId.systemDefault()).toInstant());
                        Date finalReturnDate = Date.from(departureDate.plusDays(duration).atZone(ZoneId.systemDefault()).toInstant());
                        Callable<Result> c = new Callable<Result>() {
                            @Override
                            public Result call() throws Exception {
                                return skiplaggedClient.getTripOptionBestPrice(departureAirport, destinationAirport, finalDepartureDate, finalReturnDate);
                            }
                        };
                        tasks.add(c);
                    }
                }
            }
        }
        ExecutorService exec = Executors.newFixedThreadPool(2);
        try {
            List<Future<Result>> results = exec.invokeAll(tasks);
            for (Future<Result> fr : results) {
                if (fr.get().getLink() != null && fr.get().getLink().length() > 0) {
                    res.add(fr.get());
                }
                if (fr.get().getFailedProxies() != null && !fr.get().getFailedProxies().isEmpty()) {
                    failedProxies.addAll(fr.get().getFailedProxies());
                }
                if (fr.get().getFailuresCount() > 0) {
                    failuresCount += fr.get().getFailuresCount();
                }
                if (fr.get().getSecondAttemptSuccessCount() > 0) {
                    secondAttemptSuccessCount += fr.get().getSecondAttemptSuccessCount();
                }
            }
        } catch (Exception e) {
        } finally {
            exec.shutdown();
        }

        if (!failedProxies.isEmpty() || failuresCount > 0 || secondAttemptSuccessCount > 0) {
            UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
            userRequestsSummary.setFailuresCount(userRequestsSummary.getFailuresCount() + failuresCount);
            userRequestsSummary.setSecondAttemptSuccessCount(userRequestsSummary.getSecondAttemptSuccessCount() + secondAttemptSuccessCount);
            if (!failedProxies.isEmpty()) {
                if (userRequestsSummary.getFailedProxies() == null) {
                    userRequestsSummary.setFailedProxies(failedProxies);
                } else {
                    userRequestsSummary.getFailedProxies().addAll(failedProxies);
                }
            }
            requestsSummaryDAL.updateOne(userRequestsSummary);
        }
        failuresCount = 0;
        secondAttemptSuccessCount = 0;
        failedProxies = new HashSet<>();

        if (res.isEmpty()) {
            return null;
        } else {
//            return res.stream().min(Result::compareTo).get();
            return res.stream().sorted(Result::compareTo).collect(Collectors.toList());
        }
    }
}
