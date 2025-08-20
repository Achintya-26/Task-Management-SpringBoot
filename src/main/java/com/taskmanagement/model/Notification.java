package com.taskmanagement.model;

import javax.persistence.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false)
    private String type; // 'info', 'success', 'warning', 'error'
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "related_team_id")
    private Long relatedTeamId;
    
    @Column(name = "related_activity_id")
    private Long relatedActivityId;
    
    @Column(name = "created_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
 // Relationships with proper serialization handling
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "notifications", "password"})
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_team_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "members", "activities"})
    private Team relatedTeam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_activity_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "team", "assignedMembers"})
    private Activity relatedActivity;
    
    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isRead = false;
    }
    
    public Notification(Long userId, String title, String message, String type) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    public Notification(Long userId, String title, String message, String type, 
                       Long relatedTeamId, Long relatedActivityId) {
        this(userId, title, message, type);
        this.relatedTeamId = relatedTeamId;
        this.relatedActivityId = relatedActivityId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Notification(Long id, Long userId, String title, String message, String type, Boolean isRead,
			Long relatedTeamId, Long relatedActivityId, LocalDateTime createdAt, LocalDateTime updatedAt, User user,
			Team relatedTeam, Activity relatedActivity) {
		super();
		this.id = id;
		this.userId = userId;
		this.title = title;
		this.message = message;
		this.type = type;
		this.isRead = isRead;
		this.relatedTeamId = relatedTeamId;
		this.relatedActivityId = relatedActivityId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.user = user;
		this.relatedTeam = relatedTeam;
		this.relatedActivity = relatedActivity;
	}

	public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Long getRelatedTeamId() {
        return relatedTeamId;
    }
    
    public void setRelatedTeamId(Long relatedTeamId) {
        this.relatedTeamId = relatedTeamId;
    }
    
    public Long getRelatedActivityId() {
        return relatedActivityId;
    }
    
    public void setRelatedActivityId(Long relatedActivityId) {
        this.relatedActivityId = relatedActivityId;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Team getRelatedTeam() {
        return relatedTeam;
    }
    
    public void setRelatedTeam(Team relatedTeam) {
        this.relatedTeam = relatedTeam;
    }
    
    public Activity getRelatedActivity() {
        return relatedActivity;
    }
    
    public void setRelatedActivity(Activity relatedActivity) {
        this.relatedActivity = relatedActivity;
    }
    
    // Utility methods
    public boolean isUnread() {
        return !this.isRead;
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsUnread() {
        this.isRead = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    // PrePersist and PreUpdate callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isRead == null) {
            this.isRead = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // toString method for debugging
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", isRead=" + isRead +
                ", relatedTeamId=" + relatedTeamId +
                ", relatedActivityId=" + relatedActivityId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        
        Notification that = (Notification) o;
        
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}