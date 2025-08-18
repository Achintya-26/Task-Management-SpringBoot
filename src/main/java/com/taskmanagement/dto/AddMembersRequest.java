package com.taskmanagement.dto;

import java.util.List;

public class AddMembersRequest {
    private List<Long> userIds;

    public AddMembersRequest() {}

    public AddMembersRequest(List<Long> userIds) {
        this.userIds = userIds;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
}