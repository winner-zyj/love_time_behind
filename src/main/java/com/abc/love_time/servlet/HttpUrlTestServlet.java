package com.abc.love_time.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/test/http-url")
public class HttpUrlTestServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>HTTP URL Test</title></head>");
        out.println("<body>");
        out.println("<h2>HTTP URL测试</h2>");
        out.println("<p>这个测试页面用于验证HTTP URL生成功能。</p>");
        out.println("<p>当前服务器信息：</p>");
        out.println("<ul>");
        out.println("<li>协议: " + request.getScheme() + "</li>");
        out.println("<li>服务器名: " + request.getServerName() + "</li>");
        out.println("<li>服务器端口: " + request.getServerPort() + "</li>");
        out.println("<li>上下文路径: " + request.getContextPath() + "</li>");
        out.println("</ul>");
        out.println("<p>完整URL示例: " + request.getScheme() + "://" + 
                   request.getServerName() + ":" + request.getServerPort() + 
                   request.getContextPath() + "/uploads/heartwall/test.jpg" + "</p>");
        out.println("</body>");
        out.println("</html>");
    }
}