package com.abc.love_time.servlet;

import com.abc.love_time.dao.FutureLetterDAO;
import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dao.CoupleRelationshipDAO;
import com.abc.love_time.dto.FutureLetterRequest;
import com.abc.love_time.dto.FutureLetterResponse;
import com.abc.love_time.entity.FutureLetter;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 未来情书Servlet
 */
public class FutureLetterServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final FutureLetterDAO futureLetterDAO = new FutureLetterDAO();
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
            System.out.println("[FutureLetterServlet] GET请求路径: " + pathInfo);

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

            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取未来情书列表
                handleGetList(request, response, out, userId);
            } else if (pathInfo.equals("/sent")) {
                // 获取已发送的情书列表
                handleGetSentList(request, response, out, userId);
            } else if (pathInfo.equals("/received")) {
                // 获取收到的情书列表
                handleGetReceivedList(request, response, out, userId);
            } else if (pathInfo.equals("/stats")) {
                // 获取统计信息
                handleGetStats(request, response, out, userId);
            } else if (pathInfo.matches("/\\d+")) {
                // 获取单个未来情书详情
                String[] parts = pathInfo.split("/");
                Long letterId = Long.parseLong(parts[1]);
                handleGetDetail(request, response, out, userId, letterId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] GET请求异常: " + e.getMessage());
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
            System.out.println("[FutureLetterServlet] POST请求路径: " + pathInfo);

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

            if (pathInfo == null || pathInfo.equals("/")) {
                // 创建未来情书
                handleCreate(request, response, out, userId);
            } else if (pathInfo.matches("/\\d+/send")) {
                // 发送未来情书（立即发送）
                String[] parts = pathInfo.split("/");
                Long letterId = Long.parseLong(parts[1]);
                handleSend(request, response, out, userId, letterId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] POST请求异常: " + e.getMessage());
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
            System.out.println("[FutureLetterServlet] PUT请求路径: " + pathInfo);

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

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // 更新未来情书
                String[] parts = pathInfo.split("/");
                Long letterId = Long.parseLong(parts[1]);
                handleUpdate(request, response, out, userId, letterId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] PUT请求异常: " + e.getMessage());
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
            System.out.println("[FutureLetterServlet] DELETE请求路径: " + pathInfo);

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

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // 删除未来情书
                String[] parts = pathInfo.split("/");
                Long letterId = Long.parseLong(parts[1]);
                handleDelete(request, response, out, userId, letterId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] DELETE请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 处理获取未来情书列表
     */
    private void handleGetList(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            String status = request.getParameter("status");
            
            List<FutureLetter> letters;
            if (status != null && !status.isEmpty()) {
                letters = futureLetterDAO.findByStatus(userId, status);
            } else {
                letters = futureLetterDAO.findBySenderId(userId);
            }

            FutureLetterResponse result = FutureLetterResponse.createListResponse(letters);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[FutureLetterServlet] 用户 " + userId + " 获取未来情书列表成功");

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 获取未来情书列表失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取未来情书列表失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取已发送的情书列表
     */
    private void handleGetSentList(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            List<FutureLetter> letters = futureLetterDAO.findByStatus(userId, "SENT");

            FutureLetterResponse result = FutureLetterResponse.createListResponse(letters);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[FutureLetterServlet] 用户 " + userId + " 获取已发送情书列表成功");

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 获取已发送情书列表失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取已发送情书列表失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取收到的情书列表
     */
    private void handleGetReceivedList(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 获取用户的情侣对方ID
            Long partnerId = coupleDAO.getPartnerId(userId);
            
            List<FutureLetter> letters = new ArrayList<>();
            
            // 查询直接指定该用户为接收者的信件
            letters.addAll(futureLetterDAO.findByReceiverId(userId));
            
            // 查询通过PARTNER方式发送给该用户的情书（由情侣对方发送的）
            if (partnerId != null) {
                letters.addAll(futureLetterDAO.findPartnerLetters(userId));
            }
            
            // 按发送时间排序
            letters.sort((a, b) -> {
                if (a.getSentAt() == null && b.getSentAt() == null) return 0;
                if (a.getSentAt() == null) return 1;
                if (b.getSentAt() == null) return -1;
                return b.getSentAt().compareTo(a.getSentAt());
            });

            FutureLetterResponse result = FutureLetterResponse.createListResponse(letters);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[FutureLetterServlet] 用户 " + userId + " 获取收到情书列表成功，共 " + letters.size() + " 封");

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 获取收到情书列表失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取收到情书列表失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取统计信息
     */
    private void handleGetStats(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            int draftCount = futureLetterDAO.findByStatus(userId, "DRAFT").size();
            int scheduledCount = futureLetterDAO.findByStatus(userId, "SCHEDULED").size();
            int sentCount = futureLetterDAO.findByStatus(userId, "SENT").size();

            FutureLetterResponse result = FutureLetterResponse.createStatsResponse(draftCount, scheduledCount, sentCount);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[FutureLetterServlet] 用户 " + userId + " 获取统计信息成功");

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 获取统计信息失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取统计信息失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取单个未来情书详情
     */
    private void handleGetDetail(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId, Long letterId) {
        try {
            FutureLetter letter = futureLetterDAO.findById(letterId);
            
            if (letter == null) {
                sendError(response, out, "情书不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 检查权限（发送者、接收者或情侣对方）
            boolean hasPermission = false;
            
            // 检查是否为发送者
            if (letter.getSenderId().equals(userId)) {
                hasPermission = true;
            }
            // 检查是否为直接接收者
            else if (letter.getReceiverId() != null && letter.getReceiverId().equals(userId)) {
                hasPermission = true;
            }
            // 检查是否为通过情侣关系发送的情书的接收者
            else if ("PARTNER".equals(letter.getDeliveryMethod())) {
                Long partnerId = coupleDAO.getPartnerId(userId);
                if (partnerId != null && partnerId.equals(letter.getSenderId())) {
                    hasPermission = true;
                }
            }
            
            if (!hasPermission) {
                sendError(response, out, "无权限查看该情书", HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // 如果是接收者查看，且状态为SENT，则更新为READ
            if ((letter.getReceiverId() != null && letter.getReceiverId().equals(userId)) || 
                ("PARTNER".equals(letter.getDeliveryMethod()) && coupleDAO.getPartnerId(userId) != null && coupleDAO.getPartnerId(userId).equals(letter.getSenderId()))) {
                if ("SENT".equals(letter.getStatus())) {
                    futureLetterDAO.updateStatus(letterId, "READ", null, new Timestamp(System.currentTimeMillis()));
                    letter.setStatus("READ");
                    letter.setReadAt(new Timestamp(System.currentTimeMillis()));
                }
            }

            FutureLetterResponse result = FutureLetterResponse.createSingleResponse(letter);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[FutureLetterServlet] 用户 " + userId + " 获取情书详情成功，ID: " + letterId);

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 获取情书详情失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取情书详情失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理创建未来情书
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            FutureLetterRequest letterRequest = gson.fromJson(sb.toString(), FutureLetterRequest.class);
            
            // 验证请求参数
            if (letterRequest.getTitle() == null || letterRequest.getTitle().trim().isEmpty()) {
                sendError(response, out, "情书标题不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (letterRequest.getContent() == null || letterRequest.getContent().trim().isEmpty()) {
                sendError(response, out, "情书内容不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (letterRequest.getScheduledDate() == null || letterRequest.getScheduledDate().trim().isEmpty()) {
                sendError(response, out, "预计发送日期不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 创建未来情书
            FutureLetter letter = new FutureLetter();
            letter.setSenderId(userId);
            letter.setReceiverId(letterRequest.getReceiverId());
            letter.setTitle(letterRequest.getTitle());
            letter.setContent(letterRequest.getContent());
            
            // 只支持PARTNER方式
            letter.setDeliveryMethod("PARTNER");
            
            letter.setScheduledDate(Date.valueOf(letterRequest.getScheduledDate()));
            letter.setScheduledTime(Time.valueOf(letterRequest.getScheduledTime() != null ? letterRequest.getScheduledTime() : "00:00:00"));
            letter.setStatus(letterRequest.getStatus() != null ? letterRequest.getStatus() : "DRAFT");
            letter.setBackgroundImage(letterRequest.getBackgroundImage());

            long letterId = futureLetterDAO.insert(letter);
            
            if (letterId > 0) {
                letter.setId(letterId);
                FutureLetterResponse result = FutureLetterResponse.createSingleResponse(letter);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[FutureLetterServlet] 用户 " + userId + " 创建未来情书成功，ID: " + letterId);
            } else {
                sendError(response, out, "创建未来情书失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 创建未来情书失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "创建未来情书失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理更新未来情书
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId, Long letterId) {
        try {
            // 检查情书是否存在且属于当前用户
            FutureLetter existingLetter = futureLetterDAO.findById(letterId);
            if (existingLetter == null) {
                sendError(response, out, "情书不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if (!existingLetter.getSenderId().equals(userId)) {
                sendError(response, out, "无权限操作该情书", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            FutureLetterRequest letterRequest = gson.fromJson(sb.toString(), FutureLetterRequest.class);
            
            // 更新未来情书
            existingLetter.setReceiverId(letterRequest.getReceiverId());
            existingLetter.setTitle(letterRequest.getTitle());
            existingLetter.setContent(letterRequest.getContent());
            
            // 只支持PARTNER方式
            existingLetter.setDeliveryMethod("PARTNER");
            
            if (letterRequest.getScheduledDate() != null) {
                existingLetter.setScheduledDate(Date.valueOf(letterRequest.getScheduledDate()));
            }
            if (letterRequest.getScheduledTime() != null) {
                existingLetter.setScheduledTime(Time.valueOf(letterRequest.getScheduledTime()));
            }
            existingLetter.setStatus(letterRequest.getStatus());
            existingLetter.setBackgroundImage(letterRequest.getBackgroundImage());

            boolean updated = futureLetterDAO.update(existingLetter);
            
            if (updated) {
                FutureLetterResponse result = FutureLetterResponse.createSingleResponse(existingLetter);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[FutureLetterServlet] 用户 " + userId + " 更新未来情书成功，ID: " + letterId);
            } else {
                sendError(response, out, "更新未来情书失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 更新未来情书失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "更新未来情书失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理发送未来情书（立即发送）
     */
    private void handleSend(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId, Long letterId) {
        try {
            // 检查情书是否存在且属于当前用户
            FutureLetter existingLetter = futureLetterDAO.findById(letterId);
            if (existingLetter == null) {
                sendError(response, out, "情书不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if (!existingLetter.getSenderId().equals(userId)) {
                sendError(response, out, "无权限操作该情书", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 更新状态为SENT
            Timestamp sentAt = new Timestamp(System.currentTimeMillis());
            boolean updated = futureLetterDAO.updateStatus(letterId, "SENT", sentAt, null);
            
            if (updated) {
                existingLetter.setStatus("SENT");
                existingLetter.setSentAt(sentAt);
                FutureLetterResponse result = FutureLetterResponse.createSingleResponse(existingLetter);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[FutureLetterServlet] 用户 " + userId + " 发送未来情书成功，ID: " + letterId);
            } else {
                sendError(response, out, "发送未来情书失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 发送未来情书失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "发送未来情书失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理删除未来情书
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId, Long letterId) {
        try {
            // 检查情书是否存在且属于当前用户
            FutureLetter existingLetter = futureLetterDAO.findById(letterId);
            if (existingLetter == null) {
                sendError(response, out, "情书不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            if (!existingLetter.getSenderId().equals(userId)) {
                sendError(response, out, "无权限操作该情书", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 软删除未来情书
            boolean deleted = futureLetterDAO.deleteById(letterId);
            
            if (deleted) {
                FutureLetterResponse result = FutureLetterResponse.success("删除未来情书成功");
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[FutureLetterServlet] 用户 " + userId + " 删除未来情书成功，ID: " + letterId);
            } else {
                sendError(response, out, "删除未来情书失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 删除未来情书失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "删除未来情书失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从请求头中获取用户code（通过JWT token）
     */
    private String getUserCodeFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("[FutureLetterServlet] Authorization请求头: " + authHeader);
        
        if (authHeader == null) {
            System.err.println("[FutureLetterServlet] 缺少Authorization请求头");
            return null;
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            System.err.println("[FutureLetterServlet] Authorization格式错误，应为 'Bearer {token}'");
            return null;
        }
        
        String token = authHeader.substring(7);
        System.out.println("[FutureLetterServlet] 解析到的token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        try {
            if (JwtUtil.validateToken(token)) {
                String userCode = JwtUtil.getOpenidFromToken(token);
                System.out.println("[FutureLetterServlet] 从token解析用户code: " + userCode);
                return userCode;
            } else {
                System.err.println("[FutureLetterServlet] Token验证失败");
            }
        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 解析token失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据用户code获取用户ID
     */
    private Long getUserIdByCode(String userCode) {
        try {
            User user = userDAO.findByOpenId(userCode);
            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            System.err.println("[FutureLetterServlet] 获取用户ID失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, PrintWriter out, String message, int statusCode) {
        FutureLetterResponse error = FutureLetterResponse.error(message);
        
        response.setStatus(statusCode);
        out.print(gson.toJson(error));
    }
}