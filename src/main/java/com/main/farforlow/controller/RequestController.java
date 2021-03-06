package com.main.farforlow.controller;

import com.main.farforlow.entity.Status;
import com.main.farforlow.entity.UserRequest;
import com.main.farforlow.entity.UserRequestsSummary;
import com.main.farforlow.entity.telegrammessage.TelegramMessage;
import com.main.farforlow.mongo.RequestDAL;
import com.main.farforlow.mongo.RequestsSummaryDAL;
import com.main.farforlow.service.Messenger;
import com.main.farforlow.service.TelegramCommandsProcessor;
import com.main.farforlow.service.UserRequestsProcessor;
import com.main.farforlow.view.ServiceMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("@accessCheck.check(#token)")
@RestController
public class RequestController {
    private RequestDAL requestDAL;
    private RequestsSummaryDAL requestsSummaryDAL;
    private TelegramCommandsProcessor telegramCommandsProcessor;
    private Messenger messenger;
    private UserRequestsProcessor userRequestsProcessor;

    @Autowired
    public RequestController(RequestDAL requestDAL, RequestsSummaryDAL requestsSummaryDAL, TelegramCommandsProcessor telegramCommandsProcessor,
                             Messenger messenger, UserRequestsProcessor userRequestsProcessor) {
        this.requestDAL = requestDAL;
        this.requestsSummaryDAL = requestsSummaryDAL;
        this.telegramCommandsProcessor = telegramCommandsProcessor;
        this.messenger = messenger;
        this.userRequestsProcessor = userRequestsProcessor;
    }

    @PreAuthorize("permitAll()")
    @PostMapping(value = "/requests")
    public void telegramMessagesHandler(@RequestBody TelegramMessage telegramMessage) {

        if (telegramMessage == null) {
            return;
        }

        if (telegramMessage.getMessage().getFrom().getBot() != null &&
                telegramMessage.getMessage().getFrom().getBot()) {
            return;
        }
        if (telegramMessage.getMessage().getChat().getType().equals("group") &&
                (telegramMessage.getMessage().getText() == null ||
                        !telegramMessage.getMessage().getText().startsWith("@farforlow_bot"))) {
            return;
        }
        if (telegramMessage.getMessage().getChat().getType().equals("group") &&
                telegramMessage.getMessage().getText().startsWith("@farforlow_bot")) {
            telegramMessage.getMessage().setText(telegramMessage.getMessage().getText().replaceAll("@farforlow_bot", "").trim());
        }

        UserRequest userRequest = null;
        switch (telegramMessage.getMessage().getText().trim()) {
            case "/stop":
                if (telegramCommandsProcessor.delete(telegramMessage)) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.STOP.text);
                }
                break;
            case "/start":
                if (telegramCommandsProcessor.create(telegramMessage)) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.DEPARTURE_CITY.text);
                } else {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.REQUST_EXISTS.text);
                }
                break;
            case "/help":
                userRequest = telegramCommandsProcessor.help(telegramMessage);
                if (userRequest == null) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(), ServiceMessages.NO_REQUSTS.text);
                } else {
                    messenger.sendMessage(userRequest.getTelegramUserId() != null ?
                            userRequest.getTelegramUserId() : userRequest.getTelegramGroupId(), userRequest.toString());
                }
                break;
            case "/users":
                if (!telegramMessage.getMessage().getFrom().getUserId().equals(System.getenv("BOT_OWNER_TELEGRAM_ID"))) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(),
                            ServiceMessages.NO_SUCH_COMMAND.text);
                } else {
                    StringBuilder users = new StringBuilder();
                    List<UserRequest> userRequests = requestDAL.getActive();
                    for (UserRequest user: userRequests) {
                        if (user.getFirstName() != null) {
                            users.append(user.getFirstName()).append(" ");
                        }
                        if (user.getLastName() != null) {
                            users.append(user.getLastName()).append(" ");
                        }
                        if (user.getUserName() != null) {
                            users.append("(").append(user.getUserName()).append(")");
                        }
                        users.append(";").append(" ");
                    }
                    messenger.sendMessage(System.getenv("BOT_OWNER_TELEGRAM_ID"), users.toString());
                }
                break;
            case "/queuing":
                if (!telegramMessage.getMessage().getFrom().getUserId().equals(System.getenv("BOT_OWNER_TELEGRAM_ID"))) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(),
                            ServiceMessages.NO_SUCH_COMMAND.text);
                } else {
                    messenger.sendMessage(System.getenv("BOT_OWNER_TELEGRAM_ID"), requestDAL.getQueuing().toString());
                }
                break;
            case "/active":
                if (!telegramMessage.getMessage().getFrom().getUserId().equals(System.getenv("BOT_OWNER_TELEGRAM_ID"))) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(),
                            ServiceMessages.NO_SUCH_COMMAND.text);
                } else {
                    messenger.sendMessage(System.getenv("BOT_OWNER_TELEGRAM_ID"), requestDAL.getActive().toString());
                }
                break;
            case "/incomplete":
                if (!telegramMessage.getMessage().getFrom().getUserId().equals(System.getenv("BOT_OWNER_TELEGRAM_ID"))) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(),
                            ServiceMessages.NO_SUCH_COMMAND.text);
                } else {
                    messenger.sendMessage(System.getenv("BOT_OWNER_TELEGRAM_ID"), requestDAL.getIncomplete().toString());
                }
                break;
            case "/summary":
                if (!telegramMessage.getMessage().getFrom().getUserId().equals(System.getenv("BOT_OWNER_TELEGRAM_ID"))) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(),
                            ServiceMessages.NO_SUCH_COMMAND.text);
                } else {
                    messenger.sendMessage(System.getenv("BOT_OWNER_TELEGRAM_ID"), requestsSummaryDAL.getOne().toString());
                }
                break;
            default:
                userRequest = telegramCommandsProcessor.update(telegramMessage);
                if (userRequest == null) {
                    messenger.sendMessage(telegramMessage.getMessage().getChat().getGroupId(),
                            ServiceMessages.NO_SUCH_COMMAND.text);
                }
                break;
        }
    }

    @GetMapping(value = "/requests")
    public ResponseEntity<List<UserRequest>> read(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getAll();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/{id}")
    public ResponseEntity<UserRequest> readOne(@RequestHeader(name = "token") String token, @PathVariable(name = "id") String id) {
        UserRequest userRequest = requestDAL.findById(id);
        return new ResponseEntity<>(userRequest, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/active")
    public ResponseEntity<List<UserRequest>> readActive(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getActive();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/incomplete")
    public ResponseEntity<List<UserRequest>> readIncomplete(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getIncomplete();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/queuing")
    public ResponseEntity<List<UserRequest>> readQueuing(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getQueuing();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/queuing/sorted")
    public ResponseEntity<List<UserRequest>> readQueuingSorted(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getQueuingSortedByCreationDate();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping(value = "/requests/deleted")
    public ResponseEntity<List<UserRequest>> readDeleted(@RequestHeader(name = "token") String token) {
        List<UserRequest> userRequests = requestDAL.getDeleted();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @DeleteMapping(value = "/requests/{id}")
    public ResponseEntity<?> delete(@RequestHeader(name = "token") String token, @PathVariable(name = "id") String id) {
        UserRequest userRequest = requestDAL.findById(id);
        if (userRequest == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            requestDAL.delete(userRequest);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping(value = "/requests/return/{id}")
    public ResponseEntity<UserRequest> returnFromDeleted(@RequestHeader(name = "token") String token, @PathVariable(name = "id") String id) {
        UserRequest userRequest = requestDAL.findById(id);
        if (userRequest == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            userRequest.setStatus(Status.ACTIVE);
            requestDAL.update(userRequest);
            UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
            if (userRequestsSummary == null) {
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            } else {
                userRequestsSummary.setCurrentQuantityOfSearches(userRequestsSummary.getCurrentQuantityOfSearches() +
                        userRequest.getRequestsQuantity());
                requestsSummaryDAL.updateOne(userRequestsSummary);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping(value = "/requests/summary")
    public ResponseEntity<UserRequestsSummary> readSummary(@RequestHeader(name = "token") String token) {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        return new ResponseEntity<>(userRequestsSummary, HttpStatus.OK);
    }

    @PostMapping(value = "/requests/maxRequests/{maxRequests}")
    public ResponseEntity<UserRequestsSummary> updateMaxRequests(@RequestHeader(name = "token") String token,
                                                                 @PathVariable(name = "maxRequests") String maxRequests) {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        if (userRequestsSummary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            try {
                int max = Integer.parseInt(maxRequests);
                userRequestsSummary.setMaxQuantityOfSearches(max);
                requestsSummaryDAL.updateOne(userRequestsSummary);
                return new ResponseEntity<>(userRequestsSummary, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            }
        }
    }

    @PostMapping(value = "/requests/currentRequests/{currentRequests}")
    public ResponseEntity<UserRequestsSummary> updateCurrentRequests(@RequestHeader(name = "token") String token,
                                                                     @PathVariable(name = "currentRequests") String currentRequests) {
        UserRequestsSummary userRequestsSummary = requestsSummaryDAL.getOne();
        if (userRequestsSummary == null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            try {
                int current = Integer.parseInt(currentRequests);
                userRequestsSummary.setCurrentQuantityOfSearches(current);
                requestsSummaryDAL.updateOne(userRequestsSummary);
                return new ResponseEntity<>(userRequestsSummary, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
            }
        }
    }

    @GetMapping(value = "/requests/start")
    public void requestPerformanceStart(@RequestHeader(name = "token") String token) {
        userRequestsProcessor.requestsCalculation();
    }
}
