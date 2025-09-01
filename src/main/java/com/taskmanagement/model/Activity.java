package com.taskmanagement.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "activities")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column
    private String priority;

    @Column(name = "target_date")
    private LocalDateTime targetDate;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActivityStatus status = ActivityStatus.PENDING;

    @Column(name = "creator_subscribed")
    private Boolean creatorSubscribed = true; // Default to true for backwards compatibility

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "members", "activities"})
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "activities"})
    private User creator;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "activity_assigned_users",
        joinColumns = @JoinColumn(name = "activity_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private Set<User> assignedMembers = new HashSet<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "activity"})
    private Set<Attachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "activity"})
    private Set<ActivityLink> links = new HashSet<>();
    
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "activity"})
    private Set<Remark> remarks = new HashSet<>();
    
    public enum ActivityStatus {
        PENDING("pending"),
        IN_PROGRESS("in-progress"),
        COMPLETED("completed"),
        ON_HOLD("on-hold");

        private final String value;

        ActivityStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ActivityStatus fromValue(String value) {
            for (ActivityStatus status : ActivityStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status value: " + value);
        }
    }


    // Constructors
    public Activity() {}

    public Activity(String name, String description, Team team, Long createdBy) {
        this.name = name;
        this.description = description;
        this.team = team;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // JPA Lifecycle callbacks as fallback
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ActivityStatus.PENDING;
        }
    }

//    public ActivityStatus getStatus() {
//		return status;
//	}
//
//	public void setStatus(ActivityStatus status) {
//		this.status = status;
//	}

	@PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<User> getAssignedMembers() {
        return assignedMembers;
    }

    public void setAssignedMembers(Set<User> assignedMembers) {
        this.assignedMembers = assignedMembers;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Set<ActivityLink> getLinks() {
        return links;
    }

    public void setLinks(Set<ActivityLink> links) {
        this.links = links;
    }

    public Set<Remark> getRemarks() {
        return remarks;
    }

    public void setRemarks(Set<Remark> remarks) {
        this.remarks = remarks;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public Boolean getCreatorSubscribed() {
        return creatorSubscribed;
    }

    public void setCreatorSubscribed(Boolean creatorSubscribed) {
        this.creatorSubscribed = creatorSubscribed;
    }
}