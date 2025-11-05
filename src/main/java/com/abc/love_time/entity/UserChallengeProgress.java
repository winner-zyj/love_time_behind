package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 用户挑战进度实体类
 */
public class UserChallengeProgress {
    private Long id;
    private Long userId;
    private Integer totalTasks;
    private Integer completedCount;
    private Integer favoritedCount;
    private Double completionRate;
    private Timestamp lastActiveAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public UserChallengeProgress() {
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

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getFavoritedCount() {
        return favoritedCount;
    }

    public void setFavoritedCount(Integer favoritedCount) {
        this.favoritedCount = favoritedCount;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Timestamp getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Timestamp lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
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
        return "UserChallengeProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", totalTasks=" + totalTasks +
                ", completedCount=" + completedCount +
                ", favoritedCount=" + favoritedCount +
                ", completionRate=" + completionRate +
                ", lastActiveAt=" + lastActiveAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}