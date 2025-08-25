package com.taskmanagement.dto;

import java.util.List;

public class CreateTeamRequest {
    private String name;
    private String description;
    private Long domainId;
    private List<Long> initialMembers;

    public CreateTeamRequest() {
    }

    public CreateTeamRequest(String name, String description, Long domainId, List<Long> initialMembers) {
        this.name = name;
        this.description = description;
        this.domainId = domainId;
        this.initialMembers = initialMembers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public List<Long> getInitialMembers() {
        return initialMembers;
    }

    public void setInitialMembers(List<Long> initialMembers) {
        this.initialMembers = initialMembers;
    }
}
