package com.abc.love_time.dto;

/**
 * 心形墙项目创建请求DTO
 */
public class HeartWallProjectRequest {
    private String projectName;
    private String description;
    private Boolean isPublic;
    private Integer maxPhotos;

    public HeartWallProjectRequest() {
    }

    // Getters and Setters
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

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getMaxPhotos() {
        return maxPhotos;
    }

    public void setMaxPhotos(Integer maxPhotos) {
        this.maxPhotos = maxPhotos;
    }
}