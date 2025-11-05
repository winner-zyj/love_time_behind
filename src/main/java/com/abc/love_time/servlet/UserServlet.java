package com.abc.love_time.servlet;

import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
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
import java.util.Map;
import java.util.UUID;

/**
 * 用户相关接口
 * POST /api/user/avatar/upload - 上传头像
 * POST /api/user/nickname/update - 更新昵称
 * GET  /api/user/info - 获取用户信息
 */
@WebServlet(name = "userServlet", value = "/api/user/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class UserServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new UserDAO();
    
    // 头像存储目录（相对于webapp根目录）
    private static final String UPLOAD_DIR = "uploads/avatars";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String requestURI = request.getRequestURI();
            String pathInfo = request.getPathInfo();
            System.out.println("[UserServlet] POST完整请求URI: " + requestURI);
            System.out.println("[UserServlet] POST请求路径信息: " + pathInfo);
            System.out.println("[UserServlet] Servlet路径: " + request.getServletPath());

            if (pathInfo != null && pathInfo.equals("/avatar/upload")) {
                // 上传头像
                handleAvatarUpload(request, response, out);
            } else if (pathInfo != null && pathInfo.equals("/nickname/update")) {
                // 更新昵称
                handleNicknameUpdate(request, response, out);
            } else {
                sendError(response, out, "无效的请求路径: " + pathInfo + " (完整URI: " + requestURI + ")", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[UserServlet] POST请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String requestURI = request.getRequestURI();
            String pathInfo = request.getPathInfo();
            System.out.println("[UserServlet] GET完整请求URI: " + requestURI);
            System.out.println("[UserServlet] GET请求路径信息: " + pathInfo);
            System.out.println("[UserServlet] Servlet路径: " + request.getServletPath());

            if (pathInfo != null && pathInfo.equals("/info")) {
                // 获取用户信息
                handleGetUserInfo(request, response, out);
            } else {
                sendError(response, out, "无效的请求路径: " + pathInfo + " (完整URI: " + requestURI + ")", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            System.err.println("[UserServlet] GET请求异常: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "服务器错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 处理头像上传
     */
    private void handleAvatarUpload(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        try {
            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

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
            String avatarUrl = request.getScheme() + "://" + 
                             request.getServerName() + ":" + 
                             request.getServerPort() + 
                             request.getContextPath() + "/" + 
                             UPLOAD_DIR + "/" + newFileName;

            // 更新数据库中的用户头像
            User user = userDAO.findByCode(userCode);
            if (user != null) {
                user.setAvatarUrl(avatarUrl);
                userDAO.update(user);
            }

            System.out.println("[UserServlet] 头像上传成功: " + avatarUrl);

            // 返回成功响应（标准JSON格式）
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "头像上传成功");
            result.put("avatarUrl", avatarUrl);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("[UserServlet] 头像上传失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "头像上传失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理昵称更新
     */
    private void handleNicknameUpdate(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        try {
            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 解析请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            
            // 检查昵称参数
            if (!jsonRequest.has("nickname") || jsonRequest.get("nickname").isJsonNull()) {
                sendError(response, out, "昵称不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            String nickname = jsonRequest.get("nickname").getAsString();
            if (nickname.trim().isEmpty()) {
                sendError(response, out, "昵称不能为空", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 查询用户
            User user = userDAO.findByCode(userCode);
            if (user == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 更新用户昵称
            user.setNickName(nickname);
            boolean success = userDAO.update(user);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "昵称更新成功" : "昵称更新失败");
            if (success) {
                result.put("user", user);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("[UserServlet] 昵称更新失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "昵称更新失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取用户信息
     */
    private void handleGetUserInfo(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        try {
            // 从token中获取用户code
            String userCode = getUserCodeFromToken(request);
            if (userCode == null) {
                sendError(response, out, "未登录或token已过期", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 查询用户
            User user = userDAO.findByCode(userCode);
            if (user == null) {
                sendError(response, out, "用户不存在", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", user);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            System.err.println("[UserServlet] 获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            sendError(response, out, "获取用户信息失败: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 从请求头中获取用户code（通过JWT token）
     */
    private String getUserCodeFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 验证token有效性
                if (JwtUtil.validateToken(token)) {
                    // 从JWT中解析用户code (openid)
                    String userCode = JwtUtil.getOpenidFromToken(token);
                    System.out.println("[UserServlet] 从token解析用户code: " + userCode);
                    return userCode;
                } else {
                    System.err.println("[UserServlet] Token验证失败");
                }
            } catch (Exception e) {
                System.err.println("[UserServlet] 解析token失败: " + e.getMessage());
            }
        }
        return null;
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
     * 发送错误响应（标准JSON格式）
     */
    private void sendError(HttpServletResponse response, PrintWriter out, String message, int statusCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        
        response.setStatus(statusCode);
        out.print(gson.toJson(error));
    }
}