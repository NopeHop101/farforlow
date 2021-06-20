package com.main.farforlow.mongo;

import com.main.farforlow.entity.Status;
import com.main.farforlow.entity.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RequestDALIml implements RequestDAL {

    private MongoTemplate mongoTemplate;

    @Autowired
    public RequestDALIml(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public UserRequest findByUserIdAndNotDeleted(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("telegramUserId").is(userId)
                .andOperator(Criteria.where("status").ne(Status.DELETED)));
        return mongoTemplate.findOne(query, UserRequest.class);
    }

    @Override
    public UserRequest findByGroupIdAndNotDeleted(String groupId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("telegramGroupId").is(groupId)
                .andOperator(Criteria.where("status").ne(Status.DELETED)));
        return mongoTemplate.findOne(query, UserRequest.class);
    }

    @Override
    public UserRequest findByUserIdAndIncomplete(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("telegramUserId").is(userId)
                .andOperator(Criteria.where("status").is(Status.INCOMPLETE)));
        return mongoTemplate.findOne(query, UserRequest.class);
    }

    @Override
    public UserRequest findByGroupIdAndIncomplete(String groupId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("telegramGroupId").is(groupId)
                .andOperator(Criteria.where("status").is(Status.INCOMPLETE)));
        return mongoTemplate.findOne(query, UserRequest.class);
    }

    @Override
    public List<UserRequest> getAll() {
        return mongoTemplate.findAll(UserRequest.class);
    }

    @Override
    public List<UserRequest> getActive() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.ACTIVE));
        return mongoTemplate.find(query, UserRequest.class);
    }

    @Override
    public List<UserRequest> getIncomplete() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.INCOMPLETE));
        return mongoTemplate.find(query, UserRequest.class);
    }

    @Override
    public List<UserRequest> getQueuing() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.QUEUING));
        return mongoTemplate.find(query, UserRequest.class);
    }

    @Override
    public List<UserRequest> getQueuingSortedByCreationDate() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.QUEUING)).with(Sort.by(new Order(Direction.ASC, "createdDate")));
        return mongoTemplate.find(query, UserRequest.class);
    }

    @Override
    public List<UserRequest> getDeleted() {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(Status.DELETED));
        return mongoTemplate.find(query, UserRequest.class);
    }

    @Override
    public UserRequest findById(String id) {
        return mongoTemplate.findById(id, UserRequest.class);
    }

    @Override
    public void delete(UserRequest userRequest) {
        mongoTemplate.remove(userRequest);
    }

    @Override
    public void update(UserRequest userRequest) {
        mongoTemplate.save(userRequest);
    }

    @Override
    public void create(UserRequest userRequest) {
        mongoTemplate.insert(userRequest);
    }
}
