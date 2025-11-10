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

@WebServlet("/api/test/image-access")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class ImageAccessTestServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "uploads/heartwall";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Image Access Test</title></head>");
        out.println("<body>");
        out.println("<h2>图片访问测试</h2>");
        out.println("<form method='post' enctype='multipart/form-data'>");
        out.println("<label for='file'>选择图片:</label>");
        out.println("<input type='file' id='file' name='file' accept='image/*' required><br><br>");
        out.println("<input type='submit' value='上传并测试访问'>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 获取上传的文件
            Part filePart = request.getPart("file");
            if (filePart == null) {
                out.println("<h3 style='color:red;'>错误: 未找到上传的文件</h3>");
                return;
            }
            
            // 获取原始文件名
            String fileName = getFileName(filePart);
            if (fileName == null || fileName.isEmpty()) {
                out.println("<h3 style='color:red;'>错误: 文件名无效</h3>");
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
            String fullUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + fileUrl;
            
            out.println("<h3 style='color:green;'>文件上传成功!</h3>");
            out.println("<p>文件路径: " + filePath + "</p>");
            out.println("<p>相对URL: " + fileUrl + "</p>");
            out.println("<p>完整URL: " + fullUrl + "</p>");
            out.println("<h4>图片预览:</h4>");
            out.println("<img src='" + fileUrl + "' style='max-width: 300px;'><br><br>");
            out.println("<a href='/love_time/api/test/image-access'>上传另一张图片</a>");
            
            System.out.println("[ImageAccessTestServlet] 文件上传成功: " + filePath);
            System.out.println("[ImageAccessTestServlet] 访问URL: " + fullUrl);
        } catch (Exception e) {
            out.println("<h3 style='color:red;'>错误: " + e.getMessage() + "</h3>");
            e.printStackTrace();
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