package com.abc.love_time.dto;

import java.sql.Timestamp;

/**
 * 未来情书详情响应DTO
 */
public class FutureLetterDetailResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private String deliveryMethod;
    private String scheduledDate;
    private String scheduledTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status;
    private Timestamp sentAt;
    private Timestamp readAt;
    private String backgroundImage;
    private Double backgroundOpacity; // 背景图片透明度 (0.0-1.0)
    private Integer backgroundWidth;  // 背景图片宽度
    private Integer backgroundHeight; // 背景图片高度

    // 构造函数
    public FutureLetterDetailResponse() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public Timestamp getReadAt() {
        return readAt;
    }

    public void setReadAt(Timestamp readAt) {
        this.readAt = readAt;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public Double getBackgroundOpacity() {
        return backgroundOpacity;
    }

    public void setBackgroundOpacity(Double backgroundOpacity) {
        this.backgroundOpacity = backgroundOpacity;
    }

    public Integer getBackgroundWidth() {
        return backgroundWidth;
    }

    public void setBackgroundWidth(Integer backgroundWidth) {
        this.backgroundWidth = backgroundWidth;
    }

    public Integer getBackgroundHeight() {
        return backgroundHeight;
    }

    public void setBackgroundHeight(Integer backgroundHeight) {
        this.backgroundHeight = backgroundHeight;
    }

    @Override
    public String toString() {
        return "FutureLetterDetailResponse{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", deliveryMethod='" + deliveryMethod + '\'' +
                ", scheduledDate='" + scheduledDate + '\'' +
                ", scheduledTime='" + scheduledTime + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", status='" + status + '\'' +
                ", sentAt=" + sentAt +
                ", readAt=" + readAt +
                ", backgroundImage='" + backgroundImage + '\'' +
                ", backgroundOpacity=" + backgroundOpacity +
                ", backgroundWidth=" + backgroundWidth +
                ", backgroundHeight=" + backgroundHeight +
                '}';
    }
}