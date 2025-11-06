package com.abc.love_time.dto;

import com.abc.love_time.entity.Trajectory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 轨迹点响应DTO
 */
public class TrajectoryResponse {
    private boolean success;
    private String message;
    private Object data;

    // 构造函数
    public TrajectoryResponse() {}

    public TrajectoryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public TrajectoryResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 静态工厂方法
    public static TrajectoryResponse success(String message) {
        return new TrajectoryResponse(true, message);
    }

    public static TrajectoryResponse success(String message, Object data) {
        return new TrajectoryResponse(true, message, data);
    }

    public static TrajectoryResponse error(String message) {
        return new TrajectoryResponse(false, message);
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
     * 创建轨迹点列表响应
     */
    public static TrajectoryResponse createListResponse(List<Trajectory> trajectories, List<Trajectory> partnerTrajectories) {
        Map<String, Object> result = new HashMap<>();
        result.put("trajectories", trajectories);
        result.put("partnerTrajectories", partnerTrajectories);
        
        return TrajectoryResponse.success("获取轨迹点列表成功", result);
    }

    /**
     * 创建只包含对方轨迹点的响应
     */
    public static TrajectoryResponse createPartnerOnlyResponse(List<Trajectory> partnerTrajectories) {
        Map<String, Object> result = new HashMap<>();
        result.put("trajectories", new ArrayList<>()); // 空的用户轨迹列表
        result.put("partnerTrajectories", partnerTrajectories);
        
        return TrajectoryResponse.success("获取对方轨迹点成功", result);
    }

    /**
     * 创建单个轨迹点响应
     */
    public static TrajectoryResponse createSingleResponse(Trajectory trajectory) {
        return TrajectoryResponse.success("操作成功", trajectory);
    }

    @Override
    public String toString() {
        return "TrajectoryResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}