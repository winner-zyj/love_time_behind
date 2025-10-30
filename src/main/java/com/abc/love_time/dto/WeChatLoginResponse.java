package com.abc.love_time.dto;

/**
 * 微信登录响应数据
 */
public class WeChatLoginResponse {
    private boolean success;
    private String message;
    private LoginData data;

    public WeChatLoginResponse(boolean success, String message, LoginData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

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

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

    public static class LoginData {
        private String token;
        private String openid;
        private String session_key;

        public LoginData(String token, String openid, String session_key) {
            this.token = token;
            this.openid = openid;
            this.session_key = session_key;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public String getSession_key() {
            return session_key;
        }

        public void setSession_key(String session_key) {
            this.session_key = session_key;
        }
    }
}
