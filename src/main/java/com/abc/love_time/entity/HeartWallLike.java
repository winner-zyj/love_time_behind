package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 心形墙点赞记录实体类
 */
public class HeartWallLike {
    private Long id;
    private Long postId;
    private Long userId;
    private Timestamp createdAt;

    public HeartWallLike() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "HeartWallLike{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}