package com.main.farforlow.entity;

import java.util.Date;

public class Result implements Comparable<Result> {
    private Integer priceGbp;
    private String link;
    private Date offerDate;

    public Integer getPriceGbp() {
        return priceGbp;
    }

    public void setPriceGbp(Integer priceGbp) {
        this.priceGbp = priceGbp;
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

    @Override
    public String toString() {
        return "Result{" +
                "price=" + priceGbp +
                ", link='" + link + '\'' +
                ", offerDate=" + offerDate +
                '}';
    }

    @Override
    public int compareTo(Result o) {
        return this.priceGbp - o.priceGbp;
    }
}
