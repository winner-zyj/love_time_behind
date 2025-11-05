package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 挑战任务实体类
 */
public class ChallengeTask {
    private Long id;
    private String taskName;
    private String taskDescription;
    private String category; // preset, custom
    private Integer taskIndex; // 预设任务的排序序号
    private Long createdBy; // 自定义任务的创建者
    private String iconUrl;
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // 用户完成记录（非数据库字段）
    private UserChallengeRecord userRecord;

    public ChallengeTask() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(Integer taskIndex) {
        this.taskIndex = taskIndex;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public UserChallengeRecord getUserRecord() {
        return userRecord;
    }

    public void setUserRecord(UserChallengeRecord userRecord) {
        this.userRecord = userRecord;
    }

    @Override
    public String toString() {
        return "ChallengeTask{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", taskDescription='" + taskDescription + '\'' +
                ", category='" + category + '\'' +
                ", taskIndex=" + taskIndex +
                ", createdBy=" + createdBy +
                ", iconUrl='" + iconUrl + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", userRecord=" + userRecord +
                '}';
    }
}