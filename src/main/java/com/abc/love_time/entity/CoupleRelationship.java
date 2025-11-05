package com.abc.love_time.entity;

import java.sql.Timestamp;

/**
 * 情侣关系实体类，对应 couple_relationships 表
 */
public class CoupleRelationship {
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String status; // pending, active, rejected, broken
    private Long initiatorId;
    private Long receiverId;
    private String relationshipName;
    private java.sql.Date anniversaryDate;
    private String requestMessage;
    private Timestamp createdAt;
    private Timestamp confirmedAt;
    private Timestamp rejectedAt;
    private Timestamp brokenAt;
    private Timestamp updatedAt;

    public CoupleRelationship() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public java.sql.Date getAnniversaryDate() {
        return anniversaryDate;
    }

    public void setAnniversaryDate(java.sql.Date anniversaryDate) {
        this.anniversaryDate = anniversaryDate;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Timestamp confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Timestamp getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Timestamp rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public Timestamp getBrokenAt() {
        return brokenAt;
    }

    public void setBrokenAt(Timestamp brokenAt) {
        this.brokenAt = brokenAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CoupleRelationship{" +
                "id=" + id +
                ", user1Id=" + user1Id +
                ", user2Id=" + user2Id +
                ", status='" + status + '\'' +
                ", initiatorId=" + initiatorId +
                ", receiverId=" + receiverId +
                ", relationshipName='" + relationshipName + '\'' +
                ", anniversaryDate=" + anniversaryDate +
                ", requestMessage='" + requestMessage + '\'' +
                ", createdAt=" + createdAt +
                ", confirmedAt=" + confirmedAt +
                ", rejectedAt=" + rejectedAt +
                ", brokenAt=" + brokenAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}