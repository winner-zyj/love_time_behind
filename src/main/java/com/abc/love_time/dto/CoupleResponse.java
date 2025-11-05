package com.abc.love_time.dto;

import com.abc.love_time.entity.CoupleRelationship;

/**
 * 情侣关系响应DTO
 */
public class CoupleResponse {
    private boolean success;
    private String message;
    private CoupleRelationship relationship;
    private boolean isCouple;
    private Long partnerId;
    private String partnerNickName;
    private String partnerAvatarUrl;

    public CoupleResponse() {
    }

    public static CoupleResponse success(String message) {
        CoupleResponse response = new CoupleResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static CoupleResponse success(String message, CoupleRelationship relationship) {
        CoupleResponse response = new CoupleResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setRelationship(relationship);
        return response;
    }

    public static CoupleResponse success(String message, Long partnerId, String partnerNickName, String partnerAvatarUrl) {
        CoupleResponse response = new CoupleResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setIsCouple(true);
        response.setPartnerId(partnerId);
        response.setPartnerNickName(partnerNickName);
        response.setPartnerAvatarUrl(partnerAvatarUrl);
        return response;
    }

    public static CoupleResponse error(String message) {
        CoupleResponse response = new CoupleResponse();
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

    public CoupleRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(CoupleRelationship relationship) {
        this.relationship = relationship;
    }

    public boolean isCouple() {
        return isCouple;
    }

    public void setIsCouple(boolean couple) {
        isCouple = couple;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerNickName() {
        return partnerNickName;
    }

    public void setPartnerNickName(String partnerNickName) {
        this.partnerNickName = partnerNickName;
    }

    public String getPartnerAvatarUrl() {
        return partnerAvatarUrl;
    }

    public void setPartnerAvatarUrl(String partnerAvatarUrl) {
        this.partnerAvatarUrl = partnerAvatarUrl;
    }
}