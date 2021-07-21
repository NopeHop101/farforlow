package com.main.farforlow.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "requestsSummary")
public class UserRequestsSummary {
    @Id
    private String id;
    private Integer maxQuantityOfSearches = Integer.parseInt(System.getenv("MAX_SEARCHES_PER_DAY"));
    private Integer currentQuantityOfSearches;
    private Set<String> failedProxies;
    private int failuresCount;
    private int secondAttemptSuccessCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCurrentQuantityOfSearches() {
        return currentQuantityOfSearches;
    }

    public void setCurrentQuantityOfSearches(Integer currentQuantityOfSearches) {
        this.currentQuantityOfSearches = currentQuantityOfSearches;
    }

    public Integer getMaxQuantityOfSearches() {
        return maxQuantityOfSearches;
    }

    public void setMaxQuantityOfSearches(Integer maxQuantityOfSearches) {
        this.maxQuantityOfSearches = maxQuantityOfSearches;
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
        return "UserRequestsSummary{" +
                "maxQuantityOfSearches=" + maxQuantityOfSearches +
                ", currentQuantityOfSearches=" + currentQuantityOfSearches +
                ", failedProxies=" + failedProxies.toString() +
                ", failuresCount=" + failuresCount +
                ", secondAttemptSuccessCount=" + secondAttemptSuccessCount +
                '}';
    }
}
