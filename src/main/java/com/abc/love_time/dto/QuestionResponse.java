package com.abc.love_time.dto;

/**
 * 问题响应DTO
 */
public class QuestionResponse {
    private Long id;
    private String questionText;
    private String category;
    private Integer orderIndex;
    private Boolean hasAnswered;  // 用户是否已回答
    private String userAnswer;    // 用户的答案（如果已回答）

    public QuestionResponse() {
    }

    public QuestionResponse(Long id, String questionText, String category, Integer orderIndex) {
        this.id = id;
        this.questionText = questionText;
        this.category = category;
        this.orderIndex = orderIndex;
        this.hasAnswered = false;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Boolean getHasAnswered() {
        return hasAnswered;
    }

    public void setHasAnswered(Boolean hasAnswered) {
        this.hasAnswered = hasAnswered;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    @Override
    public String toString() {
        return "QuestionResponse{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", category='" + category + '\'' +
                ", orderIndex=" + orderIndex +
                ", hasAnswered=" + hasAnswered +
                ", userAnswer='" + userAnswer + '\'' +
                '}';
    }
}
