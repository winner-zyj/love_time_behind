package com.abc.love_time.dto;

import com.abc.love_time.entity.HeartWallProject;
import com.abc.love_time.entity.HeartWallPhoto;

import java.util.List;

/**
 * 心形墙响应DTO
 */
public class HeartWallResponse {
    private boolean success;
    private String message;
    private HeartWallProject project;
    private HeartWallPhoto photo;
    private List<HeartWallProject> projects;
    private List<HeartWallPhoto> photos;
    private int photoCount;
    private int nextPosition;

    public HeartWallResponse() {
    }

    public static HeartWallResponse success(String message) {
        HeartWallResponse response = new HeartWallResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static HeartWallResponse success(String message, HeartWallProject project) {
        HeartWallResponse response = new HeartWallResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setProject(project);
        return response;
    }

    public static HeartWallResponse success(String message, HeartWallPhoto photo) {
        HeartWallResponse response = new HeartWallResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPhoto(photo);
        return response;
    }

    public static HeartWallResponse success(String message, List<HeartWallProject> projects) {
        HeartWallResponse response = new HeartWallResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setProjects(projects);
        return response;
    }

    public static HeartWallResponse success(String message, List<HeartWallPhoto> photos, int photoCount) {
        HeartWallResponse response = new HeartWallResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setPhotos(photos);
        response.setPhotoCount(photoCount);
        return response;
    }

    public static HeartWallResponse error(String message) {
        HeartWallResponse response = new HeartWallResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HeartWallProject getProject() {
        return project;
    }

    public void setProject(HeartWallProject project) {
        this.project = project;
    }

    public HeartWallPhoto getPhoto() {
        return photo;
    }

    public void setPhoto(HeartWallPhoto photo) {
        this.photo = photo;
    }

    public List<HeartWallProject> getProjects() {
        return projects;
    }

    public void setProjects(List<HeartWallProject> projects) {
        this.projects = projects;
    }

    public List<HeartWallPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<HeartWallPhoto> photos) {
        this.photos = photos;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public int getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(int nextPosition) {
        this.nextPosition = nextPosition;
    }
}