package com.main.farforlow.service;

import com.main.farforlow.entity.BotMessage;
import com.main.farforlow.view.ServiceMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class Messenger {

    private final String url = "https://api.telegram.org/bot" + System.getenv("BOT_TOKEN") + "/sendMessage";
    private final RestTemplate restTemplate;

    @Autowired
    public Messenger(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void sendMessage(String userId, String text) {
        try {
            BotMessage botMessage = new BotMessage();
            botMessage.setUserid(userId);
            botMessage.setText(text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<BotMessage> entity = new HttpEntity<>(botMessage, headers);
            ResponseEntity<?> response = this.restTemplate.postForEntity(url, entity, BotMessage.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Messenger messenger = new Messenger(new RestTemplateBuilder());
        messenger.sendMessage(System.getenv("BOT_OWNER_TELEGRAM_ID"), ServiceMessages.NO_BOTS.text);
    }
}
