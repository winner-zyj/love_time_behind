package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 用户挑战记录实体类
 */
public class UserChallengeRecord {
    private Long id;
    private Long userId;
    private Long taskId;
    private String status; // pending, completed
    private String photoUrl;
    private String note;
    private Boolean isFavorited;
    private Timestamp completedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public UserChallengeRecord() {
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

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserChallengeRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskId=" + taskId +
                ", status='" + status + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", note='" + note + '\'' +
                ", isFavorited=" + isFavorited +
                ", completedAt=" + completedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}