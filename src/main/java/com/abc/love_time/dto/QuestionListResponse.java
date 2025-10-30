package com.abc.love_time.dto;

import java.util.List;

/**
 * 问题列表响应DTO
 */
public class QuestionListResponse {
    private List<QuestionResponse> presetQuestions;   // 预设问题列表
    private List<QuestionResponse> customQuestions;   // 自定义问题列表
    private Integer totalCount;                       // 总问题数
    private Integer completedCount;                   // 已完成数
    private Long currentQuestionId;                   // 当前问题ID

    public QuestionListResponse() {
    }

    public List<QuestionResponse> getPresetQuestions() {
        return presetQuestions;
    }

    public void setPresetQuestions(List<QuestionResponse> presetQuestions) {
        this.presetQuestions = presetQuestions;
    }

    public List<QuestionResponse> getCustomQuestions() {
        return customQuestions;
    }

    public void setCustomQuestions(List<QuestionResponse> customQuestions) {
        this.customQuestions = customQuestions;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Long getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(Long currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }

    @Override
    public String toString() {
        return "QuestionListResponse{" +
                "presetQuestions=" + presetQuestions +
                ", customQuestions=" + customQuestions +
                ", totalCount=" + totalCount +
                ", completedCount=" + completedCount +
                ", currentQuestionId=" + currentQuestionId +
                '}';
    }
}
