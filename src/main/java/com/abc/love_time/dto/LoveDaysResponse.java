package com.abc.love_time.dto;

/**
 * 相爱天数响应DTO
 */
public class LoveDaysResponse {
    private boolean success;
    private String message;
    private LoveDaysData data;

    // 构造函数
    public LoveDaysResponse() {}

    public LoveDaysResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoveDaysResponse(boolean success, String message, LoveDaysData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法
    public static LoveDaysResponse success(String message, LoveDaysData data) {
        return new LoveDaysResponse(true, message, data);
    }

    public static LoveDaysResponse error(String message) {
        return new LoveDaysResponse(false, message);
    }

    // Getter和Setter方法
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

    public LoveDaysData getData() {
        return data;
    }

    public void setData(LoveDaysData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LoveDaysResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     * 相爱天数数据类
     */
    public static class LoveDaysData {
        private int loveDays;
        private String anniversaryDate;
        private String relationshipName;

        public LoveDaysData() {}

        public LoveDaysData(int loveDays, String anniversaryDate, String relationshipName) {
            this.loveDays = loveDays;
            this.anniversaryDate = anniversaryDate;
            this.relationshipName = relationshipName;
        }

        // Getter和Setter方法
        public int getLoveDays() {
            return loveDays;
        }

        public void setLoveDays(int loveDays) {
            this.loveDays = loveDays;
        }

        public String getAnniversaryDate() {
            return anniversaryDate;
        }

        public void setAnniversaryDate(String anniversaryDate) {
            this.anniversaryDate = anniversaryDate;
        }

        public String getRelationshipName() {
            return relationshipName;
        }

        public void setRelationshipName(String relationshipName) {
            this.relationshipName = relationshipName;
        }

        @Override
        public String toString() {
            return "LoveDaysData{" +
                    "loveDays=" + loveDays +
                    ", anniversaryDate='" + anniversaryDate + '\'' +
                    ", relationshipName='" + relationshipName + '\'' +
                    '}';
        }
    }
}