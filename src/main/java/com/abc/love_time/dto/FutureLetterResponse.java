package com.abc.love_time.dto;

import com.abc.love_time.entity.FutureLetter;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 未来情书响应DTO
 */
public class FutureLetterResponse {
    private boolean success;
    private String message;
    private Object data;

    // 构造函数
    public FutureLetterResponse() {}

    public FutureLetterResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public FutureLetterResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法
    public static FutureLetterResponse success(String message) {
        return new FutureLetterResponse(true, message);
    }

    public static FutureLetterResponse success(String message, Object data) {
        return new FutureLetterResponse(true, message, data);
    }

    public static FutureLetterResponse error(String message) {
        return new FutureLetterResponse(false, message);
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 创建未来情书列表响应
     */
    public static FutureLetterResponse createListResponse(List<FutureLetter> letters) {
        return FutureLetterResponse.success("获取未来情书列表成功", letters);
    }

    /**
     * 创建单个未来情书响应
     */
    public static FutureLetterResponse createSingleResponse(FutureLetter letter) {
        return FutureLetterResponse.success("操作成功", letter);
    }

    /**
     * 创建统计信息响应
     */
    public static FutureLetterResponse createStatsResponse(int draftCount, int scheduledCount, int sentCount) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("draftCount", draftCount);
        stats.put("scheduledCount", scheduledCount);
        stats.put("sentCount", sentCount);
        
        return FutureLetterResponse.success("获取统计信息成功", stats);
    }

    @Override
    public String toString() {
        return "FutureLetterResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}