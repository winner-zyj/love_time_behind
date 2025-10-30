package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 答案实体类，对应 answers 表
 */
public class Answer {
    private Long id;
    private Long questionId;
    private Long userId;
    private String answerText;
    private Timestamp answeredAt;
    private Timestamp updatedAt;

    public Answer() {
    }

    public Answer(Long questionId, Long userId, String answerText) {
        this.questionId = questionId;
        this.userId = userId;
        this.answerText = answerText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Timestamp getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Timestamp answeredAt) {
        this.answeredAt = answeredAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", userId=" + userId +
                ", answerText='" + answerText + '\'' +
                ", answeredAt=" + answeredAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
