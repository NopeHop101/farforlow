package com.main.farforlow.entity;

import java.util.Date;
import java.util.Set;

public class Result implements Comparable<Result> {
    private Integer price;
    private String currency;
    private String link;
    private Date offerDate;

    private Set<String> failedProxies;
    private int failuresCount;
    private int secondAttemptSuccessCount;

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getOfferDate() {
        return offerDate;
    }

    public void setOfferDate(Date offerDate) {
        this.offerDate = offerDate;
    }

    public Set<String> getFailedProxies() {
        return failedProxies;
    }

    public void setFailedProxies(Set<String> failedProxies) {
        this.failedProxies = failedProxies;
    }

    public int getFailuresCount() {
        return failuresCount;
    }

    public void setFailuresCount(int failuresCount) {
        this.failuresCount = failuresCount;
    }

    public int getSecondAttemptSuccessCount() {
        return secondAttemptSuccessCount;
    }

    public void setSecondAttemptSuccessCount(int secondAttemptSuccessCount) {
        this.secondAttemptSuccessCount = secondAttemptSuccessCount;
    }

    @Override
    public String toString() {
        return "Result{" +
                "price=" + price +
                ", currency='" + currency + '\'' +
                ", link='" + link + '\'' +
                ", offerDate=" + offerDate +
                '}';
    }

    @Override
    public int compareTo(Result o) {
        return this.price - o.price;
    }
}
