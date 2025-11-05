package com.abc.love_time.entity;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * 心形墙照片实体类
 */
public class HeartWallPhoto {
    private Long id;
    private Long projectId;
    private Long userId;
    private String photoUrl;
    private String thumbnailUrl;
    private Integer positionIndex;
    private String caption;
    private Date takenDate;
    private Timestamp uploadedAt;
    private Timestamp updatedAt;
    
    // 非数据库字段，用于传输用户信息
    private String userNickName;
    private String userAvatarUrl;

    public HeartWallPhoto() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(Integer positionIndex) {
        this.positionIndex = positionIndex;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Date getTakenDate() {
        return takenDate;
    }

    public void setTakenDate(Date takenDate) {
        this.takenDate = takenDate;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
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
        return "HeartWallPhoto{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", photoUrl='" + photoUrl + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", positionIndex=" + positionIndex +
                ", caption='" + caption + '\'' +
                ", takenDate=" + takenDate +
                ", uploadedAt=" + uploadedAt +
                ", updatedAt=" + updatedAt +
                ", userNickName='" + userNickName + '\'' +
                ", userAvatarUrl='" + userAvatarUrl + '\'' +
                '}';
    }
}