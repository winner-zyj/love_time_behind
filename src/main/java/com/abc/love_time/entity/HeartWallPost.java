package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 心形墙帖子实体类
 */
public class HeartWallPost {
    private Long id;
    private Long userId;
    private String content;
    private String mediaUrl; // 图片或视频URL
    private String mediaType; // IMAGE, VIDEO
    private Integer likesCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // 非数据库字段，用于传输用户信息
    private String userNickName;
    private String userAvatarUrl;

    public HeartWallPost() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
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

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    @Override
    public String toString() {
        return "HeartWallPost{" +
                "id=" + id +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", likesCount=" + likesCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", userNickName='" + userNickName + '\'' +
                ", userAvatarUrl='" + userAvatarUrl + '\'' +
                '}';
    }
}