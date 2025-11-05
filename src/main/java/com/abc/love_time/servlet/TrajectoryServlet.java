package com.abc.love_time.servlet;

import com.abc.love_time.dao.TrajectoryDAO;
import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dao.CoupleRelationshipDAO;
import com.abc.love_time.dto.TrajectoryRequest;
import com.abc.love_time.dto.TrajectoryResponse;
import com.abc.love_time.entity.Trajectory;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.DBUtil;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 轨迹点Servlet
 */
@WebServlet(name = "trajectoryServlet", value = "/api/trajectory/*")
public class TrajectoryServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final TrajectoryDAO trajectoryDAO = new TrajectoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[TrajectoryServlet] GET请求路径: " + pathInfo);

            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            System.out.println("[TrajectoryServlet] 从token获取的用户code: " + userCode);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 获取用户ID
            Long userId = getUserIdByCode(userCode);
            System.out.println("[TrajectoryServlet] 获取到的用户ID: " + userId);
            if (userId == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo != null && pathInfo.equals("/list")) {
                // 获取轨迹点列表
                handleGetList(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/location/current")) {
                // 获取双方实时位置
                handleGetCurrentLocation(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] GET请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[TrajectoryServlet] POST请求路径: " + pathInfo);

            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            System.out.println("[TrajectoryServlet] 从token获取的用户code: " + userCode);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 获取用户ID
            Long userId = getUserIdByCode(userCode);
            System.out.println("[TrajectoryServlet] 获取到的用户ID: " + userId);
            if (userId == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo != null && pathInfo.equals("/create")) {
                // 创建轨迹点
                handleCreate(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/location/update")) {
                // 上传/更新用户位置
                handleUpdateLocation(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] POST请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[TrajectoryServlet] PUT请求路径: " + pathInfo);

            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 获取用户ID
            Long userId = getUserIdByCode(userCode);
            if (userId == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo != null && pathInfo.matches("/update/\\d+")) {
                // 更新轨迹点
                String[] parts = pathInfo.split("/");
                Long trajectoryId = Long.parseLong(parts[2]);
                handleUpdate(request, response, out, userId, trajectoryId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] PUT请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[TrajectoryServlet] DELETE请求路径: " + pathInfo);

            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 获取用户ID
            Long userId = getUserIdByCode(userCode);
            if (userId == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo != null && pathInfo.matches("/delete/\\d+")) {
                // 删除轨迹点
                String[] parts = pathInfo.split("/");
                Long trajectoryId = Long.parseLong(parts[2]);
                handleDelete(request, response, out, userId, trajectoryId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] DELETE请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 处理获取轨迹点列表
     */
    private void handleGetList(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 获取查询参数
            String period = request.getParameter("period");
            int daysBack = 7; // 默认7天
            
            if ("30days".equals(period)) {
                daysBack = 30;
            } else if ("all".equals(period)) {
                daysBack = 365; // 一年
            }

            // 获取用户自己的轨迹点
            List<Trajectory> userTrajectories = trajectoryDAO.findByUserId(userId, daysBack);
            
            // 获取情侣的共享轨迹点
            List<Trajectory> partnerTrajectories = List.of();
            Long partnerId = coupleDAO.getPartnerId(userId);
            if (partnerId != null) {
                partnerTrajectories = trajectoryDAO.findSharedByPartnerId(partnerId, daysBack);
            }

            // 构造响应
            TrajectoryResponse result = TrajectoryResponse.createListResponse(userTrajectories, partnerTrajectories);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[TrajectoryServlet] 用户 " + userId + " 获取轨迹点列表成功");

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 获取轨迹点列表失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取轨迹点列表失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理创建轨迹点
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            TrajectoryRequest trajectoryRequest = gson.fromJson(sb.toString(), TrajectoryRequest.class);
            
            // 验证请求参数
            if (trajectoryRequest.getLatitude() == null || trajectoryRequest.getLongitude() == null) {
                sendError(response, out, "经纬度不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 创建轨迹点
            Trajectory trajectory = new Trajectory();
            trajectory.setUserId(userId);
            trajectory.setLatitude(trajectoryRequest.getLatitude());
            trajectory.setLongitude(trajectoryRequest.getLongitude());
            trajectory.setAddress(trajectoryRequest.getAddress());
            trajectory.setPlaceName(trajectoryRequest.getPlaceName());
            trajectory.setDescription(trajectoryRequest.getDescription());
            trajectory.setPhotoUrl(trajectoryRequest.getPhotoUrl());
            trajectory.setIsShared(trajectoryRequest.getIsShared());

            long trajectoryId = trajectoryDAO.insert(trajectory);
            
            if (trajectoryId > 0) {
                trajectory.setId(trajectoryId);
                TrajectoryResponse result = TrajectoryResponse.createSingleResponse(trajectory);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[TrajectoryServlet] 用户 " + userId + " 创建轨迹点成功，ID: " + trajectoryId);
            } else {
                sendError(response, out, "创建轨迹点失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 创建轨迹点失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "创建轨迹点失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理更新轨迹点
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId, Long trajectoryId) {
        try {
            // 检查轨迹点是否存在且属于当前用户
            Trajectory existingTrajectory = trajectoryDAO.findById(trajectoryId);
            if (existingTrajectory == null) {
                sendError(response, out, "轨迹点不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if (!existingTrajectory.getUserId().equals(userId)) {
                sendError(response, out, "无权限操作该轨迹点", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            TrajectoryRequest trajectoryRequest = gson.fromJson(sb.toString(), TrajectoryRequest.class);
            
            // 更新轨迹点
            existingTrajectory.setLatitude(trajectoryRequest.getLatitude());
            existingTrajectory.setLongitude(trajectoryRequest.getLongitude());
            existingTrajectory.setAddress(trajectoryRequest.getAddress());
            existingTrajectory.setPlaceName(trajectoryRequest.getPlaceName());
            existingTrajectory.setDescription(trajectoryRequest.getDescription());
            existingTrajectory.setPhotoUrl(trajectoryRequest.getPhotoUrl());
            existingTrajectory.setIsShared(trajectoryRequest.getIsShared());

            boolean updated = trajectoryDAO.update(existingTrajectory);
            
            if (updated) {
                TrajectoryResponse result = TrajectoryResponse.createSingleResponse(existingTrajectory);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[TrajectoryServlet] 用户 " + userId + " 更新轨迹点成功，ID: " + trajectoryId);
            } else {
                sendError(response, out, "更新轨迹点失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 更新轨迹点失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "更新轨迹点失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理删除轨迹点
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId, Long trajectoryId) {
        try {
            // 检查轨迹点是否存在且属于当前用户
            Trajectory existingTrajectory = trajectoryDAO.findById(trajectoryId);
            if (existingTrajectory == null) {
                sendError(response, out, "轨迹点不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if (!existingTrajectory.getUserId().equals(userId)) {
                sendError(response, out, "无权限操作该轨迹点", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 删除轨迹点
            boolean deleted = trajectoryDAO.deleteById(trajectoryId);
            
            if (deleted) {
                TrajectoryResponse result = TrajectoryResponse.success("删除轨迹点成功");
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[TrajectoryServlet] 用户 " + userId + " 删除轨迹点成功，ID: " + trajectoryId);
            } else {
                sendError(response, out, "删除轨迹点失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 删除轨迹点失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "删除轨迹点失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理上传/更新用户位置
     */
    private void handleUpdateLocation(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            TrajectoryRequest locationRequest = gson.fromJson(sb.toString(), TrajectoryRequest.class);
            
            // 验证请求参数
            if (locationRequest.getLatitude() == null || locationRequest.getLongitude() == null) {
                sendError(response, out, "经纬度不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 创建或更新用户当前位置（使用特殊标记表示这是实时位置）
            Trajectory currentLocation = new Trajectory();
            currentLocation.setUserId(userId);
            currentLocation.setLatitude(locationRequest.getLatitude());
            currentLocation.setLongitude(locationRequest.getLongitude());
            currentLocation.setAddress(locationRequest.getAddress());
            currentLocation.setPlaceName(locationRequest.getPlaceName());
            currentLocation.setDescription("实时位置"); // 特殊标记
            currentLocation.setIsShared(true); // 默认共享给情侣

            long locationId = trajectoryDAO.insert(currentLocation);
            
            if (locationId > 0) {
                currentLocation.setId(locationId);
                TrajectoryResponse result = TrajectoryResponse.createSingleResponse(currentLocation);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[TrajectoryServlet] 用户 " + userId + " 更新实时位置成功，ID: " + locationId);
            } else {
                sendError(response, out, "更新实时位置失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 更新实时位置失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "更新实时位置失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取双方实时位置
     */
    private void handleGetCurrentLocation(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 获取用户自己的最新位置
            Trajectory myLocation = getLatestLocation(userId);
            
            // 获取情侣的最新位置
            Trajectory partnerLocation = null;
            Long partnerId = coupleDAO.getPartnerId(userId);
            if (partnerId != null) {
                partnerLocation = getLatestLocation(partnerId);
            }

            // 计算双方距离（如果都有位置信息）
            Double distance = null;
            if (myLocation != null && partnerLocation != null) {
                distance = calculateDistance(
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    partnerLocation.getLatitude(), partnerLocation.getLongitude()
                );
            }

            // 构造响应数据
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("myLocation", myLocation);
            locationData.put("partnerLocation", partnerLocation);
            locationData.put("distance", distance);

            TrajectoryResponse result = TrajectoryResponse.success("获取实时位置成功", locationData);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[TrajectoryServlet] 用户 " + userId + " 获取实时位置成功");

        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 获取实时位置失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取实时位置失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户的最新位置
     */
    private Trajectory getLatestLocation(Long userId) {
        try {
            // 使用DAO方法获取用户最新的轨迹点作为当前位置
            return trajectoryDAO.findLatestByUserId(userId);
        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 获取用户最新位置失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 将ResultSet映射为Trajectory对象
     */
    private Trajectory mapResultSetToTrajectory(ResultSet rs) throws SQLException {
        Trajectory trajectory = new Trajectory();
        trajectory.setId(rs.getLong("id"));
        trajectory.setUserId(rs.getLong("user_id"));
        trajectory.setLatitude(rs.getDouble("latitude"));
        trajectory.setLongitude(rs.getDouble("longitude"));
        trajectory.setAddress(rs.getString("address"));
        trajectory.setPlaceName(rs.getString("place_name"));
        trajectory.setDescription(rs.getString("description"));
        trajectory.setPhotoUrl(rs.getString("photo_url"));
        trajectory.setIsShared(rs.getBoolean("is_shared"));
        trajectory.setCreatedAt(rs.getTimestamp("created_at"));
        trajectory.setUpdatedAt(rs.getTimestamp("updated_at"));
        return trajectory;
    }

    /**
     * 计算两点间的距离（使用Haversine公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // * 1000 convert to meters
        
        return distance; // 返回公里
    }

    /**
     * 从请求头中获取用户code（通过JWT token）
     */
    private String getUserCodeFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("[TrajectoryServlet] Authorization请求头: " + authHeader);
        
        if (authHeader == null) {
            System.err.println("[TrajectoryServlet] 缺少Authorization请求头");
            return null;
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            System.err.println("[TrajectoryServlet] Authorization格式错误，应为 'Bearer {token}'");
            return null;
        }
        
        String token = authHeader.substring(7);
        System.out.println("[TrajectoryServlet] 解析到的token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        try {
            if (JwtUtil.validateToken(token)) {
                String userCode = JwtUtil.getOpenidFromToken(token);
                System.out.println("[TrajectoryServlet] 从token解析用户code: " + userCode);
                return userCode;
            } else {
                System.err.println("[TrajectoryServlet] Token验证失败");
            }
        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 解析token失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据用户code获取用户ID
     */
    private Long getUserIdByCode(String userCode) {
        try {
            System.out.println("[TrajectoryServlet] 尝试根据openid查找用户: " + userCode);
            // 使用findByOpenId而不是findByCode，因为token中存储的是openid而不是code
            User user = userDAO.findByOpenId(userCode);
            if (user != null) {
                System.out.println("[TrajectoryServlet] 找到用户，ID: " + user.getId());
                return user.getId();
            } else {
                System.err.println("[TrajectoryServlet] 未找到用户，openid: " + userCode);
            }
        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 获取用户ID失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, PrintWriter out, String message, int statusCode) {
        TrajectoryResponse error = TrajectoryResponse.error(message);
        
        response.setStatus(statusCode);
        out.print(gson.toJson(error));
    }
}