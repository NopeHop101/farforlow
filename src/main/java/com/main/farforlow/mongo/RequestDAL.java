package com.main.farforlow.mongo;

import com.main.farforlow.entity.UserRequest;

import java.util.List;

public interface RequestDAL {
    UserRequest findByUserIdAndNotDeleted(String userId);

    UserRequest findByGroupIdAndNotDeleted(String groupId);

    UserRequest findByUserIdAndIncomplete(String userId);

    UserRequest findByGroupIdAndIncomplete(String groupId);

    List<UserRequest> getAll();

    List<UserRequest> getActive();

    List<UserRequest> getIncomplete();

    List<UserRequest> getQueuing();

    List<UserRequest> getQueuingSortedByCreationDate();

    List<UserRequest> getDeleted();

    UserRequest findById(String id);

    void delete(UserRequest userRequest);

    void update(UserRequest userRequest);

    void create(UserRequest userRequest);
}
