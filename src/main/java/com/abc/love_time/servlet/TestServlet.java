package com.abc.love_time.servlet;

import com.abc.love_time.util.DBUtil;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口 - 检查后端和数据库连接
 */
@WebServlet(name = "testServlet", value = "/api/test/connection")
public class TestServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("[测试] 开始测试数据库连接...");
            
            // 测试数据库连接
            try (Connection conn = DBUtil.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("[测试] 数据库连接成功！");
                    
                    // 执行简单查询测试
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users")) {
                        
                        if (rs.next()) {
                            int userCount = rs.getInt("count");
                            System.out.println("[测试] 数据库查询成功，用户表有 " + userCount + " 条记录");
                            
                            result.put("success", true);
                            result.put("message", "后端和数据库连接正常");
                            result.put("database", "连接成功");
                            result.put("userCount", userCount);
                            result.put("timestamp", System.currentTimeMillis());
                            
                            response.setStatus(HttpServletResponse.SC_OK);
                        }
                    }
                } else {
                    throw new Exception("数据库连接为空或已关闭");
                }
            }
            
        } catch (Exception e) {
            System.err.println("[测试] 连接失败: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "连接失败");
            result.put("error", e.getMessage());
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.print(gson.toJson(result));
        out.flush();
        out.close();
    }
}
