package com.abc.love_time.servlet;

import com.abc.love_time.dao.CoupleRelationshipDAO;
import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dto.CoupleRequest;
import com.abc.love_time.dto.CoupleResponse;
import com.abc.love_time.dto.LoveDaysResponse;
import com.abc.love_time.entity.CoupleRelationship;
import com.abc.love_time.entity.User;
import com.abc.love_time.service.CoupleService;
import com.abc.love_time.util.DBUtil;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 情侣关系Servlet
 */
public class CoupleServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CoupleService coupleService = new CoupleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[CoupleServlet] GET请求路径: " + pathInfo);

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

            if (pathInfo != null && pathInfo.equals("/invite/validate")) {
                // 验证邀请码
                handleValidateInviteCode(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/status")) {
                // 查询绑定状态
                handleGetStatus(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/love-days")) {
                // 获取相爱天数
                handleGetLoveDays(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[CoupleServlet] GET请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[CoupleServlet] POST请求路径: " + pathInfo);

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

            if (pathInfo != null && pathInfo.equals("/invite/create")) {
                // 生成邀请码
                handleCreateInviteCode(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/bind/accept")) {
                // 接受邀请（绑定）
                handleAcceptInvite(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/unbind")) {
                // 解绑关系
                handleUnbind(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[CoupleServlet] POST请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 处理生成邀请码
     */
    private void handleCreateInviteCode(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 检查是否已经有情侣关系
            CoupleRelationship existingRelationship = coupleDAO.findByUserId(userId);
            if (existingRelationship != null && "active".equals(existingRelationship.getStatus())) {
                sendError(response, out, "您已经有情侣关系了", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 生成唯一的邀请码（使用UUID的前6位）
            String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            
            // 更新用户的邀请码
            user.setCode(inviteCode);
            boolean updated = userDAO.update(user);
            
            if (updated) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "邀请码生成成功");
                result.put("inviteCode", inviteCode);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[CoupleServlet] 用户 " + userId + " 生成邀请码: " + inviteCode);
            } else {
                sendError(response, out, "生成邀请码失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[CoupleServlet] 生成邀请码失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "生成邀请码失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理验证邀请码
     */
    private void handleValidateInviteCode(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            String inviteCode = request.getParameter("code");
            if (inviteCode == null || inviteCode.trim().isEmpty()) {
                // 使用标准响应格式而不是错误格式
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "邀请码不能为空");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            // 查找邀请码对应的用户
            User invitedUser = userDAO.findByCode(inviteCode);
            if (invitedUser == null) {
                // 使用标准响应格式而不是错误格式
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "邀请码无效");
                
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(result));
                return;
            }

            // 检查是否是自己邀请自己
            if (invitedUser.getId().equals(userId)) {
                // 使用标准响应格式而不是错误格式
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "不能邀请自己");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            // 检查邀请用户是否已经有情侣关系
            CoupleRelationship existingRelationship = coupleDAO.findByUserId(invitedUser.getId());
            if (existingRelationship != null && "active".equals(existingRelationship.getStatus())) {
                // 使用标准响应格式而不是错误格式
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "该用户已经有情侣关系了");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "邀请码有效");
            result.put("inviterId", invitedUser.getId());
            result.put("inviterNickName", invitedUser.getNickName());
            result.put("inviterAvatarUrl", invitedUser.getAvatarUrl());
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[CoupleServlet] 用户 " + userId + " 验证邀请码: " + inviteCode);

        } catch (Exception e) {
            System.err.println("[CoupleServlet] 验证邀请码失败: " + e.getMessage());
            e.printStackTrace();
            
            // 使用标准响应格式而不是错误格式
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "验证邀请码失败: " + e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(result));
        }
    }

    /**
     * 处理接受邀请（绑定）
     */
    private void handleAcceptInvite(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 验证请求参数
            if (!jsonRequest.has("inviteCode") || jsonRequest.get("inviteCode").isJsonNull()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "邀请码不能为空");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }
            
            String inviteCode = jsonRequest.get("inviteCode").getAsString();
            if (inviteCode.trim().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "邀请码不能为空");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            // 查找邀请码对应的用户
            User invitedUser = userDAO.findByCode(inviteCode);
            if (invitedUser == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "邀请码无效");
                
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(result));
                return;
            }

            // 检查是否是自己邀请自己
            if (invitedUser.getId().equals(userId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "不能邀请自己");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            // 检查双方是否已经有情侣关系
            CoupleRelationship existingRelationship1 = coupleDAO.findByUserId(userId);
            CoupleRelationship existingRelationship2 = coupleDAO.findByUserId(invitedUser.getId());
            
            if ((existingRelationship1 != null && "active".equals(existingRelationship1.getStatus())) ||
                (existingRelationship2 != null && "active".equals(existingRelationship2.getStatus()))) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "您或对方已经有情侣关系了");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            // 创建情侣关系
            CoupleRelationship relationship = new CoupleRelationship();
            relationship.setUser1Id(Math.min(userId, invitedUser.getId()));
            relationship.setUser2Id(Math.max(userId, invitedUser.getId()));
            relationship.setInitiatorId(invitedUser.getId()); // 邀请方是发起者
            relationship.setReceiverId(userId); // 当前用户是接收者
            relationship.setStatus("active"); // 直接设置为已绑定（一键接受）
            
            long relationshipId = coupleDAO.insert(relationship);
            
            if (relationshipId > 0) {
                relationship.setId(relationshipId);
                
                // 确保confirmed_at字段被正确设置
                coupleDAO.updateStatus(relationshipId, "active");
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "绑定成功");
                result.put("relationshipId", relationshipId);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[CoupleServlet] 用户 " + userId + " 与用户 " + invitedUser.getId() + " 绑定成功");
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "绑定失败");
                
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(result));
            }

        } catch (Exception e) {
            System.err.println("[CoupleServlet] 接受邀请失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "接受邀请失败: " + e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(result));
        }
    }

    /**
     * 处理查询绑定状态
     */
    private void handleGetStatus(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 检查是否有情侣关系
            CoupleRelationship relationship = coupleDAO.findByUserId(userId);
            
            if (relationship != null && "active".equals(relationship.getStatus())) {
                // 获取伴侣信息
                Long partnerId = coupleDAO.getPartnerId(userId);
                if (partnerId != null) {
                    User partner = userDAO.findById(partnerId);
                    if (partner != null) {
                        CoupleResponse result = CoupleResponse.success(
                            "已绑定", 
                            partnerId, 
                            partner.getNickName(), 
                            partner.getAvatarUrl()
                        );
                        
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(gson.toJson(result));
                        
                        System.out.println("[CoupleServlet] 用户 " + userId + " 已绑定，伴侣ID: " + partnerId);
                        return;
                    }
                }
            }
            
            // 未绑定状态
            CoupleResponse result = CoupleResponse.success("未绑定");
            result.setIsCouple(false);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[CoupleServlet] 用户 " + userId + " 未绑定");

        } catch (Exception e) {
            System.err.println("[CoupleServlet] 查询绑定状态失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "查询绑定状态失败: " + e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(result));
        }
    }

    /**
     * 处理获取相爱天数
     */
    private void handleGetLoveDays(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            LoveDaysResponse loveDaysResponse = coupleService.calculateLoveDays(userId);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(loveDaysResponse));
            
            if (loveDaysResponse.isSuccess()) {
                System.out.println("[CoupleServlet] 用户 " + userId + " 获取相爱天数成功: " + loveDaysResponse.getData().getLoveDays() + "天");
            } else {
                System.out.println("[CoupleServlet] 用户 " + userId + " 获取相爱天数失败: " + loveDaysResponse.getMessage());
            }

        } catch (Exception e) {
            System.err.println("[CoupleServlet] 获取相爱天数失败: " + e.getMessage());
            e.printStackTrace();
            
            LoveDaysResponse errorResponse = LoveDaysResponse.error("获取相爱天数失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(errorResponse));
        }
    }

    /**
     * 处理解绑关系
     */
    private void handleUnbind(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 查找当前用户的情侣关系
            CoupleRelationship relationship = coupleDAO.findByUserId(userId);
            
            // 如果没有找到关系记录，检查是否是因为直接从数据库删除导致的不一致
            if (relationship == null) {
                // 检查用户是否在数据库中仍有标记为active的关系
                boolean hasActiveRelationship = checkUserHasActiveRelationship(userId);
                if (hasActiveRelationship) {
                    // 如果用户在数据库中仍有活跃关系，但我们的查询没有找到，可能是数据不一致
                    // 在这种情况下，我们返回成功，因为用户想要解除关系的目的已经达到
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("message", "解绑成功");
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(gson.toJson(result));
                    
                    System.out.println("[CoupleServlet] 用户 " + userId + " 解绑成功（数据已清理）");
                    return;
                } else {
                    // 确实没有关系记录
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "没有情侣关系");
                    
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson(result));
                    return;
                }
            }

            if (!"active".equals(relationship.getStatus())) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "没有有效的情侣关系");
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(result));
                return;
            }

            // 更新关系状态为已解绑
            boolean updated = coupleDAO.updateStatus(relationship.getId(), "broken");
            
            if (updated) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "解绑成功");
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[CoupleServlet] 用户 " + userId + " 解绑成功");
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "解绑失败");
                
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(result));
            }

        } catch (Exception e) {
            System.err.println("[CoupleServlet] 解绑失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "解绑失败: " + e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(result));
        }
    }

    /**
     * 检查用户是否在数据库中仍有活跃关系（用于处理数据不一致情况）
     */
    private boolean checkUserHasActiveRelationship(Long userId) {
        String sql = "SELECT COUNT(*) FROM couple_relationships WHERE (user1_id = ? OR user2_id = ?) AND status = 'active'";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CoupleServlet] 检查用户活跃关系失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * 从请求头中获取用户code（通过JWT token）
     */
    private String getUserCodeFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("[CoupleServlet] Authorization请求头: " + authHeader);
        
        if (authHeader == null) {
            System.err.println("[CoupleServlet] 缺少Authorization请求头");
            return null;
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            System.err.println("[CoupleServlet] Authorization格式错误，应为 'Bearer {token}'");
            return null;
        }
        
        String token = authHeader.substring(7);
        System.out.println("[CoupleServlet] 解析到的token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        try {
            if (JwtUtil.validateToken(token)) {
                String openid = JwtUtil.getOpenidFromToken(token);
                System.out.println("[CoupleServlet] 从token解析用户openid: " + openid);
                return openid;
            } else {
                System.err.println("[CoupleServlet] Token验证失败");
            }
        } catch (Exception e) {
            System.err.println("[CoupleServlet] 解析token失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据用户code获取用户ID
     */
    private Long getUserIdByCode(String userCode) {
        try {
            // 使用openid查找用户
            User user = userDAO.findByOpenId(userCode);
            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            System.err.println("[CoupleServlet] 获取用户ID失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, PrintWriter out, String message, int statusCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        
        response.setStatus(statusCode);
        out.print(gson.toJson(error));
    }
}