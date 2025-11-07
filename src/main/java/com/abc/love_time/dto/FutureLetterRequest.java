package com.abc.love_time.dto;

/**
 * 未来情书请求DTO
 */
public class FutureLetterRequest {
    private Long id;
    private Long receiverId;
    private String title;
    private String content;
    private String deliveryMethod; // 当前只支持PARTNER（情侣）
    private String scheduledDate; // YYYY-MM-DD格式
    private String scheduledTime; // HH:MM:SS格式
    private String status; // DRAFT, SCHEDULED, SENT, READ, CANCELLED
    private String backgroundImage;
    private Double backgroundOpacity; // 背景图片透明度 (0.0-1.0)
    private Integer backgroundWidth;  // 背景图片宽度
    private Integer backgroundHeight; // 背景图片高度

    // 构造函数
    public FutureLetterRequest() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        return "FutureLetterRequest{" +
                "id=" + id +
                ", receiverId=" + receiverId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", deliveryMethod='" + deliveryMethod + '\'' +
                ", scheduledDate='" + scheduledDate + '\'' +
                ", scheduledTime='" + scheduledTime + '\'' +
                ", status='" + status + '\'' +
                ", backgroundImage='" + backgroundImage + '\'' +
                ", backgroundOpacity=" + backgroundOpacity +
                ", backgroundWidth=" + backgroundWidth +
                ", backgroundHeight=" + backgroundHeight +
                '}';
    }
}