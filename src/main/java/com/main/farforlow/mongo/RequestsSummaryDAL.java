package com.main.farforlow.mongo;

import com.main.farforlow.entity.UserRequestsSummary;

public interface RequestsSummaryDAL {
    UserRequestsSummary getOne();

    void createOne(UserRequestsSummary userRequestsSummary);

    void updateOne(UserRequestsSummary userRequestsSummary);
}
