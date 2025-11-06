package com.abc.love_time.servlet;

import com.abc.love_time.dao.TrajectoryDAO;
import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dao.CoupleRelationshipDAO;
import com.abc.love_time.dto.TrajectoryRequest;
import com.abc.love_time.dto.TrajectoryResponse;
import com.abc.love_time.entity.Trajectory;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

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

            if (pathInfo != null && pathInfo.equals("/list")) {
                // 获取轨迹点列表
                handleGetList(request, response, out, userId);
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

            if (pathInfo != null && pathInfo.equals("/create")) {
                // 创建轨迹点
                handleCreate(request, response, out, userId);
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
            String showPartnerOnly = request.getParameter("showPartnerOnly"); // 新增参数，用于控制是否只显示对方轨迹
            String startDate = request.getParameter("startDate"); // 新增参数，日期筛选开始日期
            String endDate = request.getParameter("endDate"); // 新增参数，日期筛选结束日期
            int daysBack = 7; // 默认7天
            
            if ("30days".equals(period)) {
                daysBack = 30;
            } else if ("all".equals(period)) {
                daysBack = 365; // 一年
            }

            List<Trajectory> userTrajectories = new ArrayList<>();
            List<Trajectory> partnerTrajectories = new ArrayList<>();
            
            // 获取情侣ID
            Long partnerId = coupleDAO.getPartnerId(userId);
            
            // 如果提供了日期范围参数，则使用日期范围查询
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                if ("true".equals(showPartnerOnly) && partnerId != null) {
                    // 只获取情侣的所有轨迹点（不仅限于共享的），按日期范围筛选
                    partnerTrajectories = trajectoryDAO.findAllByPartnerIdAndDateRange(partnerId, startDate, endDate);
                    // 构造只包含对方轨迹点的响应
                    TrajectoryResponse result = TrajectoryResponse.createPartnerOnlyResponse(partnerTrajectories);
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(result));
                } else {
                    // 获取用户自己的轨迹点，按日期范围筛选
                    userTrajectories = trajectoryDAO.findByUserIdAndDateRange(userId, startDate, endDate);
                    
                    // 获取情侣的共享轨迹点，按日期范围筛选
                    if (partnerId != null) {
                        partnerTrajectories = trajectoryDAO.findSharedByPartnerIdAndDateRange(partnerId, startDate, endDate);
                    }

                    // 构造响应
                    TrajectoryResponse result = TrajectoryResponse.createListResponse(userTrajectories, partnerTrajectories);
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(result));
                }
            } else {
                // 使用原有的天数回溯查询方式
                if ("true".equals(showPartnerOnly) && partnerId != null) {
                    // 只获取情侣的所有轨迹点（不仅限于共享的）
                    partnerTrajectories = trajectoryDAO.findAllByPartnerId(partnerId, daysBack);
                    // 构造只包含对方轨迹点的响应
                    TrajectoryResponse result = TrajectoryResponse.createPartnerOnlyResponse(partnerTrajectories);
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(result));
                } else {
                    // 获取用户自己的轨迹点
                    userTrajectories = trajectoryDAO.findByUserId(userId, daysBack);
                    
                    // 获取情侣的共享轨迹点
                    if (partnerId != null) {
                        partnerTrajectories = trajectoryDAO.findSharedByPartnerId(partnerId, daysBack);
                    }

                    // 构造响应
                    TrajectoryResponse result = TrajectoryResponse.createListResponse(userTrajectories, partnerTrajectories);
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(result));
                }
            }
            
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
            User user = userDAO.findByCode(userCode);
            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            System.err.println("[TrajectoryServlet] 获取用户ID失败: " + e.getMessage());
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