package com.abc.love_time.dto;

/**
 * 情侣关系请求DTO
 */
public class CoupleRequest {
    private String inviteCode;
    private String requestMessage;
    private String relationshipName;
    private java.sql.Date anniversaryDate;

    public CoupleRequest() {
    }

    // Getters and Setters
    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
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
}