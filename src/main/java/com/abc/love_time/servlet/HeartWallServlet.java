package com.abc.love_time.servlet;

import com.abc.love_time.dao.HeartWallProjectDAO;
import com.abc.love_time.dao.HeartWallPhotoDAO;
import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dao.CoupleRelationshipDAO; // 添加情侣关系DAO
import com.abc.love_time.dto.HeartWallProjectRequest;
import com.abc.love_time.dto.HeartWallPhotoRequest;
import com.abc.love_time.dto.HeartWallResponse;
import com.abc.love_time.entity.HeartWallProject;
import com.abc.love_time.entity.HeartWallPhoto;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.JwtUtil;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class HeartWallServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private HeartWallProjectDAO projectDAO = new HeartWallProjectDAO();
    private HeartWallPhotoDAO photoDAO = new HeartWallPhotoDAO();
    private UserDAO userDAO = new UserDAO();
    private CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO(); // 添加情侣关系DAO
    
    // 心形墙照片存储目录（相对于webapp根目录）
    private static final String UPLOAD_DIR = "uploads/heartwall";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            HeartWallResponse errorResponse = HeartWallResponse.error("未提供有效的认证令牌");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        try {
            String jwtToken = token.substring(7);
            // 验证token有效性
            if (!JwtUtil.validateToken(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            String openid = JwtUtil.getOpenidFromToken(jwtToken);
            // 在实际应用中，您需要通过openid查询数据库获取用户ID
            // 这里为了简化，我们假设openid就是用户ID
            Long userId = getUserIdByCode(openid); // 使用实际的用户ID获取方法
            
            if (openid == null || userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            if ("/projects".equals(pathInfo)) {
                createProject(request, response, userId);
            } else if ("/photos".equals(pathInfo)) {
                uploadPhoto(request, response, userId);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                response.getWriter().write(gson.toJson(errorResponse));
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            HeartWallResponse errorResponse = HeartWallResponse.error("未提供有效的认证令牌");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        try {
            String jwtToken = token.substring(7);
            // 验证token有效性
            if (!JwtUtil.validateToken(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            String openid = JwtUtil.getOpenidFromToken(jwtToken);
            // 在实际应用中，您需要通过openid查询数据库获取用户ID
            // 这里为了简化，我们假设openid就是用户ID
            Long userId = getUserIdByCode(openid); // 使用实际的用户ID获取方法
            
            if (openid == null || userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            if (pathInfo == null || "/projects".equals(pathInfo)) {
                getUserProjects(request, response, userId);
            } else if (pathInfo.startsWith("/projects/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3) {
                    Long projectId = Long.parseLong(parts[2]);
                    if ("photos".equals(request.getParameter("action"))) {
                        getProjectPhotos(request, response, projectId, userId);
                    } else {
                        getProjectDetail(request, response, projectId, userId);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                    response.getWriter().write(gson.toJson(errorResponse));
                }
            } else if ("/next-position".equals(pathInfo)) {
                getNextPosition(request, response, userId);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                response.getWriter().write(gson.toJson(errorResponse));
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            HeartWallResponse errorResponse = HeartWallResponse.error("未提供有效的认证令牌");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        try {
            String jwtToken = token.substring(7);
            // 验证token有效性
            if (!JwtUtil.validateToken(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            String openid = JwtUtil.getOpenidFromToken(jwtToken);
            // 在实际应用中，您需要通过openid查询数据库获取用户ID
            // 这里为了简化，我们假设openid就是用户ID
            Long userId = getUserIdByCode(openid); // 使用实际的用户ID获取方法
            
            if (openid == null || userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            if (pathInfo != null && pathInfo.startsWith("/projects/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3) {
                    Long projectId = Long.parseLong(parts[2]);
                    updateProject(request, response, projectId, userId);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                    response.getWriter().write(gson.toJson(errorResponse));
                }
            } else if (pathInfo != null && pathInfo.startsWith("/photos/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3) {
                    Long photoId = Long.parseLong(parts[2]);
                    updatePhoto(request, response, photoId, userId);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                    response.getWriter().write(gson.toJson(errorResponse));
                }
            } else if ("/clear-photos".equals(pathInfo)) {
                clearProjectPhotos(request, response, userId);
            } else if ("/projects/clear-photos".equals(pathInfo)) {
                clearProjectPhotos(request, response, userId);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                response.getWriter().write(gson.toJson(errorResponse));
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            HeartWallResponse errorResponse = HeartWallResponse.error("未提供有效的认证令牌");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        try {
            String jwtToken = token.substring(7);
            // 验证token有效性
            if (!JwtUtil.validateToken(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            String openid = JwtUtil.getOpenidFromToken(jwtToken);
            // 在实际应用中，您需要通过openid查询数据库获取用户ID
            // 这里为了简化，我们假设openid就是用户ID
            Long userId = getUserIdByCode(openid); // 使用实际的用户ID获取方法
            
            if (openid == null || userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的认证令牌");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            
            if (pathInfo != null && pathInfo.startsWith("/projects/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3) {
                    Long projectId = Long.parseLong(parts[2]);
                    deleteProject(request, response, projectId, userId);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                    response.getWriter().write(gson.toJson(errorResponse));
                }
            } else if (pathInfo != null && pathInfo.startsWith("/photos/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3) {
                    Long photoId = Long.parseLong(parts[2]);
                    deletePhoto(request, response, photoId, userId);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                    response.getWriter().write(gson.toJson(errorResponse));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                response.getWriter().write(gson.toJson(errorResponse));
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }
    
    private void createProject(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException, SQLException {
        HeartWallProjectRequest projectRequest = gson.fromJson(request.getReader(), HeartWallProjectRequest.class);
        
        if (projectRequest.getProjectName() == null || projectRequest.getProjectName().trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("项目名称不能为空");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        HeartWallProject project = new HeartWallProject();
        project.setUserId(userId);
        project.setProjectName(projectRequest.getProjectName());
        project.setDescription(projectRequest.getDescription());
        project.setPublic(projectRequest.getIsPublic() != null ? projectRequest.getIsPublic() : false);
        project.setMaxPhotos(projectRequest.getMaxPhotos() != null ? projectRequest.getMaxPhotos() : 40);
        
        project = projectDAO.insert(project);
        
        // 成功时返回200状态码而不是201
        response.setStatus(HttpServletResponse.SC_OK);
        HeartWallResponse successResponse = HeartWallResponse.success("心形墙项目创建成功", project);
        response.getWriter().write(gson.toJson(successResponse));
    }
    
    private void uploadPhoto(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException, SQLException {
        try {
            // 检查是否为multipart请求（文件上传）
            String contentType = request.getContentType();
            if (contentType != null && contentType.startsWith("multipart/")) {
                // 处理文件上传
                handleFileUpload(request, response, userId);
            } else {
                // 处理JSON请求（原有逻辑）
                handleJsonUpload(request, response, userId);
            }
        } catch (Exception e) {
            handleError(response, e);
        }
    }
    
    private void handleFileUpload(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException, SQLException, ServletException {
        // 获取项目ID参数
        String projectIdStr = request.getParameter("projectId");
        if (projectIdStr == null || projectIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("项目ID不能为空");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        Long projectId;
        try {
            projectId = Long.parseLong(projectIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("项目ID格式不正确");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查项目是否存在且属于该用户或其情侣
        HeartWallProject project = projectDAO.findById(projectId);
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限在此项目中上传照片（自己或情侣）
        if (!hasPermissionForProject(project, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限在此项目中上传照片");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查项目是否已满
        if (project.getPhotoCount() != null && project.getPhotoCount() >= project.getMaxPhotos()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("该项目已达到最大照片数量限制");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 获取位置索引参数（可选）
        String positionIndexStr = request.getParameter("positionIndex");
        int positionIndex;
        if (positionIndexStr != null && !positionIndexStr.isEmpty()) {
            try {
                positionIndex = Integer.parseInt(positionIndexStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                HeartWallResponse errorResponse = HeartWallResponse.error("位置索引格式不正确");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
        } else {
            // 如果未指定位置，获取下一个可用位置
            positionIndex = photoDAO.getNextAvailablePosition(project.getId());
        }
        
        if (positionIndex == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("没有可用的位置索引");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查位置是否在有效范围内
        if (positionIndex < 1 || positionIndex > project.getMaxPhotos()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("位置索引超出有效范围");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 获取上传的文件
        Part filePart = request.getPart("file");
        if (filePart == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("未找到上传的文件");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 获取原始文件名
        String fileName = getFileName(filePart);
        if (fileName == null || fileName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("文件名无效");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 验证文件类型
        String fileContentType = filePart.getContentType();
        if (!isValidImageType(fileContentType)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("仅支持图片格式（jpg, jpeg, png, gif）");
            response.getWriter().write(gson.toJson(errorResponse));
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
        
        // 生成相对路径（只存储相对路径到数据库）
        String relativePath = UPLOAD_DIR + "/" + newFileName;
        
        // 生成完整URL用于返回给前端
        String fullUrl = request.getScheme() + "://" + 
                         request.getServerName() + ":" + 
                         request.getServerPort() + 
                         request.getContextPath() + "/" + 
                         relativePath;
        
        // 打印调试信息
        System.out.println("[HeartWallServlet] 上传文件路径: " + filePath);
        System.out.println("[HeartWallServlet] 存储到数据库的相对路径: " + relativePath);
        System.out.println("[HeartWallServlet] 返回给前端的完整URL: " + fullUrl);
        
        // 获取照片说明（可选）
        String caption = request.getParameter("caption");
        
        // 获取拍摄日期（可选）
        String takenDateStr = request.getParameter("takenDate");
        java.sql.Date takenDate = null;
        if (takenDateStr != null && !takenDateStr.isEmpty()) {
            try {
                takenDate = java.sql.Date.valueOf(takenDateStr);
            } catch (Exception e) {
                // 如果日期格式不正确，忽略该参数
            }
        }
        
        HeartWallPhoto photo = new HeartWallPhoto();
        photo.setProjectId(projectId);
        photo.setUserId(userId);
        photo.setPhotoUrl(relativePath);  // 数据库中存储相对路径
        photo.setThumbnailUrl(relativePath);  // 缩略图URL暂时与原图相同
        photo.setPositionIndex(positionIndex);
        photo.setCaption(caption);
        photo.setTakenDate(takenDate);
        
        try {
            photo = photoDAO.insert(photo);
            
            // 设置返回给前端的完整URL
            photo.setPhotoUrl(fullUrl);
            photo.setThumbnailUrl(fullUrl);
            
            // 成功时返回200状态码
            response.setStatus(HttpServletResponse.SC_OK);
            
            // 构造符合前端要求的响应格式
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", createPhotoDataMap(photo));
            
            response.getWriter().write(gson.toJson(result));
        } catch (SQLException e) {
            // 出现异常时返回500状态码
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "服务器内部错误: " + e.getMessage());
            response.getWriter().write(gson.toJson(errorResult));
        }
    }
    
    private void handleJsonUpload(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException, SQLException {
        HeartWallPhotoRequest photoRequest = gson.fromJson(request.getReader(), HeartWallPhotoRequest.class);
        
        if (photoRequest.getProjectId() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("项目ID不能为空");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查项目是否存在且属于该用户或其情侣
        HeartWallProject project = projectDAO.findById(photoRequest.getProjectId());
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限在此项目中上传照片（自己或情侣）
        if (!hasPermissionForProject(project, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限在此项目中上传照片");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查项目是否已满
        if (project.getPhotoCount() != null && project.getPhotoCount() >= project.getMaxPhotos()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("该项目已达到最大照片数量限制");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 如果未指定位置，获取下一个可用位置
        int positionIndex = photoRequest.getPositionIndex() != null ? photoRequest.getPositionIndex() : 
                           photoDAO.getNextAvailablePosition(project.getId());
        
        if (positionIndex == -1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("没有可用的位置索引");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查位置是否在有效范围内
        if (positionIndex < 1 || positionIndex > project.getMaxPhotos()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("位置索引超出有效范围");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        HeartWallPhoto photo = new HeartWallPhoto();
        photo.setProjectId(photoRequest.getProjectId());
        photo.setUserId(userId);
        photo.setPhotoUrl(photoRequest.getPhotoUrl());
        photo.setThumbnailUrl(photoRequest.getThumbnailUrl());
        photo.setPositionIndex(positionIndex);
        photo.setCaption(photoRequest.getCaption());
        photo.setTakenDate(photoRequest.getTakenDate());
        
        try {
            photo = photoDAO.insert(photo);
            // 成功时返回200状态码而不是201
            response.setStatus(HttpServletResponse.SC_OK);
            HeartWallResponse successResponse = HeartWallResponse.success("照片上传成功", photo);
            response.getWriter().write(gson.toJson(successResponse));
        } catch (SQLException e) {
            // 出现异常时返回500状态码
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            HeartWallResponse errorResponse = HeartWallResponse.error("服务器内部错误: " + e.getMessage());
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    private void getUserProjects(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException, SQLException {
        // 获取情侣ID
        Long partnerId = coupleDAO.getPartnerId(userId);
        
        // 获取用户自己和情侣的项目
        List<HeartWallProject> projects = projectDAO.findByUserIdOrPartnerId(userId, partnerId);
        
        // 设置每个项目的用户信息和是否属于情侣的标识
        for (HeartWallProject project : projects) {
            User user = userDAO.findById(project.getUserId());
            if (user != null) {
                project.setUserNickName(user.getNickName());
                project.setUserAvatarUrl(user.getAvatarUrl());
            }
            
            // 如果项目不属于当前用户，标记为情侣项目
            if (!project.getUserId().equals(userId)) {
                project.setIsPartnerProject(true);
            }
        }
        
        HeartWallResponse successResponse = HeartWallResponse.success("获取用户心形墙项目列表成功", projects);
        response.getWriter().write(gson.toJson(successResponse));
    }
    
    private void getProjectDetail(HttpServletRequest request, HttpServletResponse response, Long projectId, Long userId) throws IOException, SQLException {
        HeartWallProject project = projectDAO.findById(projectId);
        
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限查看此项目（自己或情侣）
        if (!hasPermissionForProject(project, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限查看此项目");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 获取用户信息
        User user = userDAO.findById(project.getUserId());
        if (user != null) {
            project.setUserNickName(user.getNickName());
            project.setUserAvatarUrl(user.getAvatarUrl());
        }
        
        HeartWallResponse successResponse = HeartWallResponse.success("获取心形墙项目详情成功", project);
        response.getWriter().write(gson.toJson(successResponse));
    }
    
    private void getProjectPhotos(HttpServletRequest request, HttpServletResponse response, Long projectId, Long userId) throws IOException, SQLException {
        HeartWallProject project = projectDAO.findById(projectId);
        
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限查看此项目（自己或情侣）
        if (!hasPermissionForProject(project, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限查看此项目");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 获取分页参数
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        int pageSize = request.getParameter("pageSize") != null ? Integer.parseInt(request.getParameter("pageSize")) : 20;
        int offset = (page - 1) * pageSize;
        
        List<HeartWallPhoto> photos = photoDAO.findByProjectIdWithLimit(projectId, offset, pageSize);
        
        // 获取照片的用户信息，并将相对路径转换为完整URL
        for (HeartWallPhoto photo : photos) {
            User user = userDAO.findById(photo.getUserId());
            if (user != null) {
                photo.setUserNickName(user.getNickName());
                photo.setUserAvatarUrl(user.getAvatarUrl());
            }
            
            // 如果photoUrl是相对路径，转换为完整URL
            String photoUrl = photo.getPhotoUrl();
            if (photoUrl != null && !photoUrl.startsWith("http")) {
                // 将相对路径转换为完整URL
                String fullUrl = request.getScheme() + "://" + 
                               request.getServerName() + ":" + 
                               request.getServerPort() + 
                               request.getContextPath() + "/" + 
                               photoUrl;
                photo.setPhotoUrl(fullUrl);
                System.out.println("[HeartWallServlet] 转换照片URL: " + photoUrl + " -> " + fullUrl);
            }
        }
        
        int photoCount = photoDAO.getPhotoCountByProjectId(projectId);
        
        HeartWallResponse successResponse = HeartWallResponse.success("获取心形墙照片列表成功", photos, photoCount);
        response.getWriter().write(gson.toJson(successResponse));
    }
    
    private void getNextPosition(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException, SQLException {
        String projectIdStr = request.getParameter("projectId");
        if (projectIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            HeartWallResponse errorResponse = HeartWallResponse.error("projectId参数不能为空");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        Long projectId = Long.parseLong(projectIdStr);
        
        // 检查项目是否存在且属于该用户或其情侣
        HeartWallProject project = projectDAO.findById(projectId);
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        if (!hasPermissionForProject(project, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限访问此项目");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        int nextPosition = photoDAO.getNextAvailablePosition(projectId);
        
        HeartWallResponse successResponse = HeartWallResponse.success("获取下一个可用位置成功");
        successResponse.setNextPosition(nextPosition);
        response.getWriter().write(gson.toJson(successResponse));
    }
    
    private void updateProject(HttpServletRequest request, HttpServletResponse response, Long projectId, Long userId) throws IOException, SQLException {
        HeartWallProject existingProject = projectDAO.findById(projectId);
        
        if (existingProject == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限修改此项目（自己或情侣）
        if (!hasPermissionForProject(existingProject, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限修改此项目");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        HeartWallProjectRequest projectRequest = gson.fromJson(request.getReader(), HeartWallProjectRequest.class);
        
        // 检查是否需要清空所有照片
        if (projectRequest.getMaxPhotos() != null && projectRequest.getMaxPhotos() == 0) {
            // 清空项目中的所有照片
            boolean cleared = projectDAO.clearAllPhotos(projectId);
            if (!cleared) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                HeartWallResponse errorResponse = HeartWallResponse.error("清空照片失败");
                response.getWriter().write(gson.toJson(errorResponse));
                return;
            }
            // 重置项目照片计数
            existingProject.setPhotoCount(0);
            projectDAO.update(existingProject);
        }
        
        if (projectRequest.getProjectName() != null) {
            existingProject.setProjectName(projectRequest.getProjectName());
        }
        
        if (projectRequest.getDescription() != null) {
            existingProject.setDescription(projectRequest.getDescription());
        }
        
        if (projectRequest.getIsPublic() != null) {
            existingProject.setPublic(projectRequest.getIsPublic());
        }
        
        boolean updated = projectDAO.update(existingProject);
        
        if (updated) {
            HeartWallResponse successResponse = HeartWallResponse.success("心形墙项目更新成功", existingProject);
            response.getWriter().write(gson.toJson(successResponse));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            HeartWallResponse errorResponse = HeartWallResponse.error("更新失败");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    private void updatePhoto(HttpServletRequest request, HttpServletResponse response, Long photoId, Long userId) throws IOException, SQLException {
        HeartWallPhoto existingPhoto = photoDAO.findById(photoId);
        
        if (existingPhoto == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "指定的照片不存在");
            response.getWriter().write(gson.toJson(errorResult));
            return;
        }
        
        // 检查用户是否有权限修改此照片（自己上传的或情侣上传的）
        HeartWallProject project = projectDAO.findById(existingPhoto.getProjectId());
        if (project == null || !hasPermissionForProject(project, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "您没有权限修改此照片");
            response.getWriter().write(gson.toJson(errorResult));
            return;
        }
        
        HeartWallPhotoRequest photoRequest = gson.fromJson(request.getReader(), HeartWallPhotoRequest.class);
        
        if (photoRequest.getPhotoUrl() != null) {
            existingPhoto.setPhotoUrl(photoRequest.getPhotoUrl());
        }
        
        if (photoRequest.getThumbnailUrl() != null) {
            existingPhoto.setThumbnailUrl(photoRequest.getThumbnailUrl());
        }
        
        if (photoRequest.getPositionIndex() != null) {
            // 检查位置是否在有效范围内
            if (photoRequest.getPositionIndex() < 1 || photoRequest.getPositionIndex() > project.getMaxPhotos()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "位置索引超出有效范围");
                response.getWriter().write(gson.toJson(errorResult));
                return;
            }
            existingPhoto.setPositionIndex(photoRequest.getPositionIndex());
        }
        
        if (photoRequest.getCaption() != null) {
            existingPhoto.setCaption(photoRequest.getCaption());
        }
        
        if (photoRequest.getTakenDate() != null) {
            existingPhoto.setTakenDate(photoRequest.getTakenDate());
        }
        
        boolean updated = photoDAO.update(existingPhoto);
        
        if (updated) {
            // 如果photoUrl是相对路径，转换为完整URL
            String photoUrl = existingPhoto.getPhotoUrl();
            if (photoUrl != null && !photoUrl.startsWith("http")) {
                // 将相对路径转换为完整URL
                String fullUrl = request.getScheme() + "://" + 
                               request.getServerName() + ":" + 
                               request.getServerPort() + 
                               request.getContextPath() + "/" + 
                               photoUrl;
                existingPhoto.setPhotoUrl(fullUrl);
            }
            
            // 如果thumbnailUrl是相对路径，转换为完整URL
            String thumbnailUrl = existingPhoto.getThumbnailUrl();
            if (thumbnailUrl != null && !thumbnailUrl.startsWith("http")) {
                // 将相对路径转换为完整URL
                String fullUrl = request.getScheme() + "://" + 
                               request.getServerName() + ":" + 
                               request.getServerPort() + 
                               request.getContextPath() + "/" + 
                               thumbnailUrl;
                existingPhoto.setThumbnailUrl(fullUrl);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", createPhotoDataMap(existingPhoto));
            response.getWriter().write(gson.toJson(result));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "更新失败");
            response.getWriter().write(gson.toJson(errorResult));
        }
    }
    
    private void deleteProject(HttpServletRequest request, HttpServletResponse response, Long projectId, Long userId) throws IOException, SQLException {
        HeartWallProject project = projectDAO.findById(projectId);
        
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限删除此项目（自己创建的）
        if (!project.getUserId().equals(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限删除此项目");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        boolean deleted = projectDAO.deleteById(projectId);
        
        if (deleted) {
            HeartWallResponse successResponse = HeartWallResponse.success("心形墙项目删除成功");
            response.getWriter().write(gson.toJson(successResponse));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            HeartWallResponse errorResponse = HeartWallResponse.error("删除失败");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    private void deletePhoto(HttpServletRequest request, HttpServletResponse response, Long photoId, Long userId) throws IOException, SQLException {
        HeartWallPhoto photo = photoDAO.findById(photoId);
        
        if (photo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的照片不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限删除此照片（自己上传的）
        if (!photo.getUserId().equals(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限删除此照片");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查项目是否存在
        HeartWallProject project = projectDAO.findById(photo.getProjectId());
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            HeartWallResponse errorResponse = HeartWallResponse.error("照片关联的项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        boolean deleted = photoDAO.deleteById(photoId);
        
        if (deleted) {
            HeartWallResponse successResponse = HeartWallResponse.success("照片删除成功");
            response.getWriter().write(gson.toJson(successResponse));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            HeartWallResponse errorResponse = HeartWallResponse.error("删除失败");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    /**
     * 清空心形墙项目中的所有照片
     */
    private void clearProjectPhotos(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException, SQLException {
        String projectIdStr = request.getParameter("projectId");
        if (projectIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "projectId参数不能为空");
            response.getWriter().write(gson.toJson(errorResult));
            return;
        }
        
        Long projectId = Long.parseLong(projectIdStr);
        
        // 检查项目是否存在且属于该用户
        HeartWallProject project = projectDAO.findById(projectId);
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResult));
            return;
        }
        
        // 检查用户是否有权限清空此项目（自己创建的）
        if (!project.getUserId().equals(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "您没有权限清空此项目");
            response.getWriter().write(gson.toJson(errorResult));
            return;
        }
        
        // 清空项目中的所有照片
        boolean cleared = projectDAO.clearAllPhotos(projectId);
        
        if (cleared) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            response.getWriter().write(gson.toJson(result));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "清空照片失败");
            response.getWriter().write(gson.toJson(errorResult));
        }
    }
    
    private void handleError(HttpServletResponse response, Exception e) throws IOException {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        HeartWallResponse errorResponse = HeartWallResponse.error("服务器内部错误: " + e.getMessage());
        response.getWriter().write(gson.toJson(errorResponse));
    }
    
    /**
     * 根据用户code获取用户ID
     * @param userCode 用户code（openid）
     * @return 用户ID
     */
    private Long getUserIdByCode(String userCode) {
        try {
            // 首先尝试使用openid查找用户
            User user = userDAO.findByOpenId(userCode);
            if (user != null) {
                return user.getId();
            }
            
            // 如果使用openid找不到，再尝试使用code查找用户
            user = userDAO.findByCode(userCode);
            if (user != null) {
                return user.getId();
            }
            
            System.err.println("[HeartWallServlet] 无法找到用户，userCode: " + userCode);
        } catch (Exception e) {
            System.err.println("[HeartWallServlet] 获取用户ID失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 检查用户是否有权限访问项目（自己创建的或情侣创建的）
     */
    private boolean hasPermissionForProject(HeartWallProject project, Long userId) {
        // 如果是用户自己创建的项目，有权限
        if (project.getUserId().equals(userId)) {
            return true;
        }
        
        // 检查是否是情侣关系
        try {
            Long partnerId = coupleDAO.getPartnerId(userId);
            if (partnerId != null && project.getUserId().equals(partnerId)) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("[HeartWallServlet] 检查情侣关系失败: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 创建符合前端要求的照片数据映射
     */
    private Map<String, Object> createPhotoDataMap(HeartWallPhoto photo) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", photo.getId());
        data.put("photoId", photo.getId());
        data.put("projectId", photo.getProjectId());
        data.put("positionIndex", photo.getPositionIndex());
        data.put("photoUrl", photo.getPhotoUrl());
        data.put("thumbnailUrl", photo.getThumbnailUrl());
        data.put("caption", photo.getCaption());
        data.put("takenDate", photo.getTakenDate());
        return data;
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
}