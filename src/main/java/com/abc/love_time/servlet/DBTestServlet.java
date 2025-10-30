package com.abc.love_time.servlet;

import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.DBUtil;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库连接测试接口
 * GET /api/db/test
 */
@WebServlet(name = "dbTestServlet", value = "/api/db/test")
public class DBTestServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("[数据库测试] 开始测试数据库连接...");

            // 1. 测试数据库连接
            boolean connected = DBUtil.testConnection();
            result.put("connected", connected);

            if (!connected) {
                result.put("success", false);
                result.put("message", "数据库连接失败");
                out.print(gson.toJson(result));
                return;
            }

            // 2. 测试查询用户
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.findAll();
            
            result.put("success", true);
            result.put("message", "数据库连接成功");
            result.put("userCount", users.size());
            result.put("users", users);

            System.out.println("[数据库测试] 测试成功，查询到 " + users.size() + " 个用户");

        } catch (Exception e) {
            System.err.println("[数据库测试] 测试失败: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "数据库测试失败: " + e.getMessage());
        }

        out.print(gson.toJson(result));
        out.flush();
        out.close();
    }
}
