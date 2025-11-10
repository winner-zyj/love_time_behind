package com.abc.love_time.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/api/test/upload-verify")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class UploadVerificationServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "uploads/heartwall";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 获取上传的文件
            Part filePart = request.getPart("file");
            if (filePart == null) {
                out.print("{\"success\": false, \"message\": \"未找到上传的文件\"}");
                return;
            }
            
            // 获取原始文件名
            String fileName = getFileName(filePart);
            if (fileName == null || fileName.isEmpty()) {
                out.print("{\"success\": false, \"message\": \"文件名无效\"}");
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
            String fileUrl = request.getContextPath() + "/" + UPLOAD_DIR + "/" + newFileName;
            
            // 验证文件是否真的保存到了磁盘
            File savedFile = new File(filePath);
            boolean fileExists = savedFile.exists();
            
            out.print("{\"success\": true, \"message\": \"文件上传" + (fileExists ? "成功" : "失败") + "\", \"fileUrl\": \"" + fileUrl + "\", \"filePath\": \"" + filePath + "\", \"fileExists\": " + fileExists + "}");
            
            System.out.println("[UploadVerificationServlet] 文件上传路径: " + filePath);
            System.out.println("[UploadVerificationServlet] 文件是否存在: " + fileExists);
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"文件上传失败: " + e.getMessage() + "\"}");
        }
    }
    
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
    
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf);
    }
}