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
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HeartWallServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private HeartWallProjectDAO projectDAO = new HeartWallProjectDAO();
    private HeartWallPhotoDAO photoDAO = new HeartWallPhotoDAO();
    private UserDAO userDAO = new UserDAO();
    private CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO(); // 添加情侣关系DAO

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
            String openid = JwtUtil.getOpenidFromToken(token.substring(7));
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
            String openid = JwtUtil.getOpenidFromToken(token.substring(7));
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
            String openid = JwtUtil.getOpenidFromToken(token.substring(7));
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
            String openid = JwtUtil.getOpenidFromToken(token.substring(7));
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
                    deleteProject(response, projectId, userId);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    HeartWallResponse errorResponse = HeartWallResponse.error("无效的请求路径");
                    response.getWriter().write(gson.toJson(errorResponse));
                }
            } else if (pathInfo != null && pathInfo.startsWith("/photos/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3) {
                    Long photoId = Long.parseLong(parts[2]);
                    deletePhoto(response, photoId, userId);
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
        List<HeartWallProject> projects = projectDAO.findByUserId(userId);
        
        // 获取每个项目的用户信息
        for (HeartWallProject project : projects) {
            User user = userDAO.findById(project.getUserId());
            if (user != null) {
                project.setUserNickName(user.getNickName());
                project.setUserAvatarUrl(user.getAvatarUrl());
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
        
        // 获取照片的用户信息
        for (HeartWallPhoto photo : photos) {
            User user = userDAO.findById(photo.getUserId());
            if (user != null) {
                photo.setUserNickName(user.getNickName());
                photo.setUserAvatarUrl(user.getAvatarUrl());
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
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的照片不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限修改此照片（自己上传的或情侣上传的）
        HeartWallProject project = projectDAO.findById(existingPhoto.getProjectId());
        if (project == null || !hasPermissionForProject(project, userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限修改此照片");
            response.getWriter().write(gson.toJson(errorResponse));
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
                HeartWallResponse errorResponse = HeartWallResponse.error("位置索引超出有效范围");
                response.getWriter().write(gson.toJson(errorResponse));
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
            HeartWallResponse successResponse = HeartWallResponse.success("照片更新成功", existingPhoto);
            response.getWriter().write(gson.toJson(successResponse));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            HeartWallResponse errorResponse = HeartWallResponse.error("更新失败");
            response.getWriter().write(gson.toJson(errorResponse));
        }
    }
    
    private void deleteProject(HttpServletResponse response, Long projectId, Long userId) throws IOException, SQLException {
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
    
    private void deletePhoto(HttpServletResponse response, Long photoId, Long userId) throws IOException, SQLException {
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
            HeartWallResponse errorResponse = HeartWallResponse.error("projectId参数不能为空");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        Long projectId = Long.parseLong(projectIdStr);
        
        // 检查项目是否存在且属于该用户
        HeartWallProject project = projectDAO.findById(projectId);
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            HeartWallResponse errorResponse = HeartWallResponse.error("指定的心形墙项目不存在");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 检查用户是否有权限清空此项目（自己创建的）
        if (!project.getUserId().equals(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            HeartWallResponse errorResponse = HeartWallResponse.error("您没有权限清空此项目");
            response.getWriter().write(gson.toJson(errorResponse));
            return;
        }
        
        // 清空项目中的所有照片
        boolean cleared = projectDAO.clearAllPhotos(projectId);
        
        if (cleared) {
            HeartWallResponse successResponse = HeartWallResponse.success("项目照片已清空");
            response.getWriter().write(gson.toJson(successResponse));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            HeartWallResponse errorResponse = HeartWallResponse.error("清空照片失败");
            response.getWriter().write(gson.toJson(errorResponse));
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
     */
    private Long getUserIdByCode(String userCode) {
        try {
            User user = userDAO.findByCode(userCode);
            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            System.err.println("[HeartWallServlet] 获取用户ID失败: " + e.getMessage());
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
}