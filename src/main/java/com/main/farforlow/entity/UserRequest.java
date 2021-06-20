package com.main.farforlow.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "requests")
public class UserRequest {
    @Id
    private String id;
    @Indexed
    private String telegramUserId;
    @Indexed
    private String telegramGroupId;
    private String firstName;
    private String lastName;
    @Indexed
    private String userName;
    private String departureCity;
    private String departureCountry;
    private String destinationCity;
    private String destinationCountry;
    private String tripDuration;
    private String searchPeriod;
    private Integer minTripDurationDays;
    private Integer maxTripDurationDays;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date earliestTripStartDate;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date latestReturnDate;
    private List<String> departureAirports;
    private List<String> destinationAirports;
    private List<Result> results;
    private Integer bestPrice;
    private Integer requestsQuantity;
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Indexed
    private Date createdDate;
    @Indexed
    private Status status;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getDepartureCountry() {
        return departureCountry;
    }

    public void setDepartureCountry(String departureCountry) {
        this.departureCountry = departureCountry;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(String tripDuration) {
        this.tripDuration = tripDuration;
    }

    public String getSearchPeriod() {
        return searchPeriod;
    }

    public void setSearchPeriod(String searchPeriod) {
        this.searchPeriod = searchPeriod;
    }

    public Integer getMinTripDurationDays() {
        return minTripDurationDays;
    }

    public void setMinTripDurationDays(Integer minTripDurationDays) {
        this.minTripDurationDays = minTripDurationDays;
    }

    public Integer getMaxTripDurationDays() {
        return maxTripDurationDays;
    }

    public void setMaxTripDurationDays(Integer maxTripDurationDays) {
        this.maxTripDurationDays = maxTripDurationDays;
    }

    public Date getEarliestTripStartDate() {
        return earliestTripStartDate;
    }

    public void setEarliestTripStartDate(Date earliestTripStartDate) {
        this.earliestTripStartDate = earliestTripStartDate;
    }

    public Date getLatestReturnDate() {
        return latestReturnDate;
    }

    public void setLatestReturnDate(Date latestReturnDate) {
        this.latestReturnDate = latestReturnDate;
    }

    public List<String> getDepartureAirports() {
        return departureAirports;
    }

    public void setDepartureAirports(List<String> departureAirports) {
        this.departureAirports = departureAirports;
    }

    public List<String> getDestinationAirports() {
        return destinationAirports;
    }

    public void setDestinationAirports(List<String> destinationAirports) {
        this.destinationAirports = destinationAirports;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Integer getBestPrice() {
        return bestPrice;
    }

    public void setBestPrice(Integer bestPrice) {
        this.bestPrice = bestPrice;
    }

    public Integer getRequestsQuantity() {
        return requestsQuantity;
    }

    public void setRequestsQuantity(Integer requestsQuantity) {
        this.requestsQuantity = requestsQuantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(String telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public String getTelegramGroupId() {
        return telegramGroupId;
    }

    public void setTelegramGroupId(String telegramGroupId) {
        this.telegramGroupId = telegramGroupId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "REQUEST " +
                "departure city:'" + departureCity + '\'' +
                ", destination city:'" + destinationCity + '\'' +
                ", trip duration:'" + tripDuration + '\'' +
                ", search period:'" + searchPeriod + '\'' +
                ", request status:'" + status + '\'';
    }
}
