package com.abc.love_time.entity;

import java.sql.Timestamp;

public class HeartWallProject {
    private Long id;
    private Long userId;
    private String projectName;
    private String description;
    private Integer photoCount;
    private Integer maxPhotos;
    private String coverPhotoUrl;
    private Boolean isPublic;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // 非数据库字段，用于传输用户信息
    private String userNickName;
    private String userAvatarUrl;
    
    // 非数据库字段，用于标识项目是否属于情侣共享
    private Boolean isPartnerProject = false;

    public HeartWallProject() {
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public Integer getMaxPhotos() {
        return maxPhotos;
    }

    public void setMaxPhotos(Integer maxPhotos) {
        this.maxPhotos = maxPhotos;
    }

    public String getCoverPhotoUrl() {
        return coverPhotoUrl;
    }

    public void setCoverPhotoUrl(String coverPhotoUrl) {
        this.coverPhotoUrl = coverPhotoUrl;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
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
    
    public Boolean getIsPartnerProject() {
        return isPartnerProject;
    }
    
    public void setIsPartnerProject(Boolean isPartnerProject) {
        this.isPartnerProject = isPartnerProject;
    }

    @Override
    public String toString() {
        return "HeartWallProject{" +
                "id=" + id +
                ", userId=" + userId +
                ", projectName='" + projectName + '\'' +
                ", description='" + description + '\'' +
                ", photoCount=" + photoCount +
                ", maxPhotos=" + maxPhotos +
                ", coverPhotoUrl='" + coverPhotoUrl + '\'' +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", userNickName='" + userNickName + '\'' +
                ", userAvatarUrl='" + userAvatarUrl + '\'' +
                ", isPartnerProject=" + isPartnerProject +
                '}';
    }
}