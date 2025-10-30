package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 用户答题进度实体类，对应 user_question_progress 表
 */
public class UserQuestionProgress {
    private Long id;
    private Long userId;
    private Long currentQuestionId;
    private Integer completedCount;
    private Integer totalCount;
    private Timestamp lastActiveAt;
    private Timestamp createdAt;

    public UserQuestionProgress() {
    }

    public UserQuestionProgress(Long userId) {
        this.userId = userId;
        this.completedCount = 0;
        this.totalCount = 0;
    }

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

    public Long getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(Long currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
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

    @Override
    public String toString() {
        return "UserQuestionProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", currentQuestionId=" + currentQuestionId +
                ", completedCount=" + completedCount +
                ", totalCount=" + totalCount +
                ", lastActiveAt=" + lastActiveAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
