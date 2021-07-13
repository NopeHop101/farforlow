package com.main.farforlow.parser;

import com.main.farforlow.entity.Result;
import com.main.farforlow.entity.UserRequest;
import com.main.farforlow.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ParserModel {

    private final SkiplaggedClient skiplaggedClient;
    private final Utils utils;

    @Autowired
    public ParserModel(SkiplaggedClient skiplaggedClient, Utils utils) {
        this.skiplaggedClient = skiplaggedClient;
        this.utils = utils;
    }

    public Result requestExecutor(UserRequest userRequest) {
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
        ExecutorService exec = Executors.newFixedThreadPool(4);
        try {
            List<Future<Result>> results = exec.invokeAll(tasks);
            for (Future<Result> fr : results) {
                if (fr.get() != null) {
                    res.add(fr.get());
                }
            }
        } catch (Exception e) {
        } finally {
            exec.shutdown();
        }
        if (res.isEmpty()) {
            return null;
        } else {
            return res.stream().min(Result::compareTo).get();
        }
    }

    public static void main(String[] args) {
        ParserModel parserModel = new ParserModel(new SkiplaggedClient(), new Utils());
        UserRequest userRequest = new UserRequest();
        userRequest.setUserName("A");
        userRequest.setRequestsQuantity(300);
        userRequest.setMinTripDurationDays(10);
        userRequest.setMaxTripDurationDays(12);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            userRequest.setEarliestTripStartDate(formatter.parse("10.11.2021"));
            userRequest.setLatestReturnDate(formatter.parse("25.11.2021"));
        } catch (Exception e) {
        }
        userRequest.setDepartureAirports(Arrays.asList("GVA"));
        userRequest.setDestinationAirports(Arrays.asList("LHR", "LCY"));
        long start = System.currentTimeMillis();
        Result result = parserModel.requestExecutor(userRequest);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(String.format("Elapsed time: %d s", elapsed / 1000));
        System.out.println(result);
    }
}
