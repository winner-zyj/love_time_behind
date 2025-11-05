package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 问题实体类，对应 questions 表
 */
public class Question {
    private Long id; //主键
    private String questionText;//问题内容
    private String category;  // preset 或 custom
    private Long createdBy; //创建时间
    private Boolean isActive;
    private Integer orderIndex;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Question() {
    }

    public Question(String questionText, String category) {
        this.questionText = questionText;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    // 为了兼容性，添加getText/setText别名方法
    public String getText() {
        return questionText;
    }

    public void setText(String text) {
        this.questionText = text;
    }

    // 为了兼容性，添加getType/setType别名方法
    public String getType() {
        return category;
    }

    public void setType(String type) {
        this.category = type;
    }

    // 为了兼容性，添加getCreatorId/setCreatorId别名方法
    public Long getCreatorId() {
        return createdBy;
    }

    public void setCreatorId(Long creatorId) {
        this.createdBy = creatorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
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
        return "Question{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", category='" + category + '\'' +
                ", createdBy=" + createdBy +
                ", isActive=" + isActive +
                ", orderIndex=" + orderIndex +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
