package com.main.farforlow.mongo;

import com.main.farforlow.entity.UserRequestsSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RequestsSummaryDALImpl implements RequestsSummaryDAL {

    private MongoTemplate mongoTemplate;

    @Autowired
    public RequestsSummaryDALImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UserRequestsSummary getOne() {
        List<UserRequestsSummary> userRequestsSummaryList = mongoTemplate.findAll(UserRequestsSummary.class);
        if (userRequestsSummaryList.isEmpty()) {
            return null;
        }
        return userRequestsSummaryList.get(0);
    }

    @Override
    public void createOne(UserRequestsSummary userRequestsSummary) {
        UserRequestsSummary current = getOne();
        if (current == null) {
            mongoTemplate.insert(userRequestsSummary);
        }
    }

    @Override
    public void updateOne(UserRequestsSummary userRequestsSummary) {
        mongoTemplate.save(userRequestsSummary);
    }
}
