package com.main.farforlow.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "requestsSummary")
public class UserRequestsSummary {
    @Id
    private String id;
    private Integer maxQuantityOfSearches = 20000;
    private Integer currentQuantityOfSearches;

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
}
