package com.abc.love_time.servlet;

import com.abc.love_time.dao.ChallengeDAO;
import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dao.CoupleRelationshipDAO; // 添加情侣关系DAO
import com.abc.love_time.entity.ChallengeTask;
import com.abc.love_time.entity.User;
import com.abc.love_time.entity.UserChallengeProgress;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 一百事挑战接口
 * GET  /api/challenge/tasks           - 获取任务列表
 * GET  /api/challenge/progress        - 获取用户进度
 * POST /api/challenge/task/add        - 添加自定义任务
 * POST /api/challenge/task/delete     - 删除自定义任务
 * POST /api/challenge/complete        - 标记任务完成/取消完成
 * POST /api/challenge/favorite        - 收藏/取消收藏任务
 * POST /api/challenge/upload          - 上传照片
 */
@WebServlet(name = "challengeServlet", value = "/api/challenge/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class ChallengeServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final ChallengeDAO challengeDAO = new ChallengeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO(); // 添加情侣关系DAO
    
    // 一百件事照片存储目录（相对于webapp根目录）
    private static final String UPLOAD_DIR = "uploads/challenge";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("[ChallengeServlet] GET请求路径: " + pathInfo);

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

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/tasks")) {
                // 获取任务列表
                handleGetTasks(request, response, out, userId);
            } else if (pathInfo.equals("/progress")) {
                // 获取用户进度
                handleGetProgress(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[ChallengeServlet] GET请求异常: " + e.getMessage());
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
            System.out.println("[ChallengeServlet] POST请求路径: " + pathInfo);

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

            if (pathInfo != null && pathInfo.equals("/task/add")) {
                // 添加自定义任务
                handleAddTask(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/task/delete")) {
                // 删除自定义任务
                handleDeleteTask(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/complete")) {
                // 标记任务完成/取消完成
                handleCompleteTask(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/favorite")) {
                // 收藏/取消收藏任务
                handleFavoriteTask(request, response, out, userId);
            } else if (pathInfo != null && pathInfo.equals("/upload")) {
                // 上传照片
                handleUploadPhoto(request, response, out, userId);
            } else {
                sendError(response, out, "无效的请求路径", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[ChallengeServlet] POST请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 处理获取任务列表
     */
    private void handleGetTasks(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            List<ChallengeTask> tasks = challengeDAO.getTasksByUser(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "获取成功");
            result.put("tasks", tasks);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[ChallengeServlet] 获取任务列表，共 " + tasks.size() + " 个任务");
        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 获取任务列表失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取任务列表失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取用户进度
     */
    private void handleGetProgress(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            UserChallengeProgress progress = challengeDAO.getProgressByUser(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "获取成功");
            result.put("progress", progress);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[ChallengeServlet] 获取用户 " + userId + " 的进度");
        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 获取用户进度失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取用户进度失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理添加自定义任务
     */
    private void handleAddTask(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 验证请求参数
            if (!jsonRequest.has("taskName") || jsonRequest.get("taskName").isJsonNull()) {
                sendError(response, out, "任务名称不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            String taskName = jsonRequest.get("taskName").getAsString();
            if (taskName.trim().isEmpty()) {
                sendError(response, out, "任务名称不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            String taskDescription = "";
            if (jsonRequest.has("taskDescription") && !jsonRequest.get("taskDescription").isJsonNull()) {
                taskDescription = jsonRequest.get("taskDescription").getAsString();
            }

            // 创建自定义任务
            ChallengeTask customTask = new ChallengeTask();
            customTask.setTaskName(taskName);
            customTask.setTaskDescription(taskDescription);
            customTask.setCreatedBy(userId);
            
            long taskId = challengeDAO.addCustomTask(customTask);
            
            if (taskId > 0) {
                customTask.setId(taskId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "任务添加成功");
                result.put("task", customTask);
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[ChallengeServlet] 用户 " + userId + " 添加任务 " + taskId + " 成功");
            } else {
                sendError(response, out, "任务添加失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 添加自定义任务失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "添加自定义任务失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理删除自定义任务
     */
    private void handleDeleteTask(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 验证请求参数
            if (!jsonRequest.has("taskId") || jsonRequest.get("taskId").isJsonNull()) {
                sendError(response, out, "任务ID不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long taskId = jsonRequest.get("taskId").getAsLong();

            // 删除自定义任务
            boolean success = challengeDAO.deleteCustomTask(taskId, userId);
            
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "任务删除成功");
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[ChallengeServlet] 用户 " + userId + " 删除任务 " + taskId + " 成功");
            } else {
                sendError(response, out, "只能删除自己创建的自定义任务", HttpServletResponse.SC_FORBIDDEN);
            }

        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 删除自定义任务失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "删除自定义任务失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理标记任务完成/取消完成
     */
    private void handleCompleteTask(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 验证请求参数
            if (!jsonRequest.has("taskId") || jsonRequest.get("taskId").isJsonNull()) {
                sendError(response, out, "任务ID不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (!jsonRequest.has("completed") || jsonRequest.get("completed").isJsonNull()) {
                sendError(response, out, "完成状态不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long taskId = jsonRequest.get("taskId").getAsLong();
            boolean completed = jsonRequest.get("completed").getAsBoolean();
            
            String photoUrl = null;
            if (jsonRequest.has("photoUrl") && !jsonRequest.get("photoUrl").isJsonNull()) {
                photoUrl = jsonRequest.get("photoUrl").getAsString();
            }
            
            String note = null;
            if (jsonRequest.has("note") && !jsonRequest.get("note").isJsonNull()) {
                note = jsonRequest.get("note").getAsString();
            }

            // 更新任务完成状态
            boolean success = challengeDAO.updateTaskCompletion(userId, taskId, completed, photoUrl, note);
            
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", completed ? "任务已完成" : "已取消完成");
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[ChallengeServlet] 用户 " + userId + " " + (completed ? "完成" : "取消完成") + "任务 " + taskId);
            } else {
                sendError(response, out, "更新任务状态失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 标记任务完成状态失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "标记任务完成状态失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理收藏/取消收藏任务
     */
    private void handleFavoriteTask(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 验证请求参数
            if (!jsonRequest.has("taskId") || jsonRequest.get("taskId").isJsonNull()) {
                sendError(response, out, "任务ID不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            if (!jsonRequest.has("favorited") || jsonRequest.get("favorited").isJsonNull()) {
                sendError(response, out, "收藏状态不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long taskId = jsonRequest.get("taskId").getAsLong();
            boolean favorited = jsonRequest.get("favorited").getAsBoolean();

            // 更新任务收藏状态
            boolean success = challengeDAO.updateTaskFavorite(userId, taskId, favorited);
            
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", favorited ? "已收藏" : "已取消收藏");
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(result));
                
                System.out.println("[ChallengeServlet] 用户 " + userId + " " + (favorited ? "收藏" : "取消收藏") + "任务 " + taskId);
            } else {
                sendError(response, out, "更新收藏状态失败", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 更新任务收藏状态失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "更新任务收藏状态失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理上传照片
     */
    private void handleUploadPhoto(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Long userId) {
        try {
            // 获取上传的文件
            Part filePart = request.getPart("file");
            if (filePart == null) {
                sendError(response, out, "未找到上传的文件", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // 获取原始文件名
            String fileName = getFileName(filePart);
            if (fileName == null || fileName.isEmpty()) {
                sendError(response, out, "文件名无效", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // 验证文件类型
            String contentType = filePart.getContentType();
            if (!isValidImageType(contentType)) {
                sendError(response, out, "仅支持图片格式（jpg, jpeg, png, gif）", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // 生成唯一文件名
            String fileExtension = getFileExtension(fileName);
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 获取上传目录的绝对路径
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 保存文件
            String filePath = uploadPath + File.separator + newFileName;
            Files.copy(filePart.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            
            // 生成访问URL
            String photoUrl = request.getScheme() + "://" + 
                             request.getServerName() + ":" + 
                             request.getServerPort() + 
                             request.getContextPath() + "/" + 
                             UPLOAD_DIR + "/" + newFileName;
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "照片上传成功");
            result.put("photoUrl", photoUrl);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));
            
            System.out.println("[ChallengeServlet] 用户 " + userId + " 上传照片成功: " + photoUrl);
        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 上传照片失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "上传照片失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取上传文件的文件名
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) return null;
        
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }
    
    /**
     * 验证是否为有效的图片类型
     */
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif")
        );
    }

    /**
     * 从请求头中获取用户code（通过JWT token）
     */
    private String getUserCodeFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("[ChallengeServlet] Authorization请求头: " + authHeader);
        
        if (authHeader == null) {
            System.err.println("[ChallengeServlet] 缺少Authorization请求头");
            return null;
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            System.err.println("[ChallengeServlet] Authorization格式错误，应为 'Bearer {token}'");
            return null;
        }
        
        String token = authHeader.substring(7);
        System.out.println("[ChallengeServlet] 解析到的token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        try {
            if (JwtUtil.validateToken(token)) {
                String userCode = JwtUtil.getOpenidFromToken(token);
                System.out.println("[ChallengeServlet] 从token解析用户code: " + userCode);
                return userCode;
            } else {
                System.err.println("[ChallengeServlet] Token验证失败");
            }
        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 解析token失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据用户code获取用户ID
     */
    private Long getUserIdByCode(String userCode) {
        try {
            // 使用findByOpenId而不是findByCode，因为userCode实际上是openid
            User user = userDAO.findByOpenId(userCode);
            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            System.err.println("[ChallengeServlet] 获取用户ID失败: " + e.getMessage());
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