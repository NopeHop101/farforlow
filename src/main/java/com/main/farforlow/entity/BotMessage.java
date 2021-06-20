package com.main.farforlow.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BotMessage {
    @JsonProperty("chat_id")
    private String userid;
    @JsonProperty("text")
    private String text;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
