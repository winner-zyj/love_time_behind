package com.abc.love_time.servlet;

import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.dto.WeChatLoginRequest;
import com.abc.love_time.dto.WeChatLoginResponse;
import com.abc.love_time.dto.WeChatSession;
import com.abc.love_time.entity.User;
import com.abc.love_time.util.JwtUtil;
import com.abc.love_time.util.WeChatApiClient;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 微信登录接口
 * POST /api/login/wechat
 */
@WebServlet(name = "weChatLoginServlet", value = "/api/login/wechat")
public class WeChatLoginServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置请求和响应的编码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            System.out.println("[微信登录] 开始处理登录请求...");
            
            // 1. 读取请求体
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            
            System.out.println("[微信登录] 接收到的请求数据: " + requestBody.toString());

            // 2. 解析请求数据
            WeChatLoginRequest loginRequest = gson.fromJson(requestBody.toString(), WeChatLoginRequest.class);
            System.out.println("[微信登录] 解析后的code: " + loginRequest.getCode());

            // 3. 验证请求参数
            if (loginRequest.getCode() == null || loginRequest.getCode().trim().isEmpty()) {
                System.out.println("[微信登录] 错误: code参数为空");
                WeChatLoginResponse errorResponse = new WeChatLoginResponse(
                        false, "code参数不能为空", null
                );
                out.print(gson.toJson(errorResponse));
                return;
            }

            // 4. 调用微信API获取openid和session_key
            System.out.println("[微信登录] 开始调用微信API...");
            WeChatSession weChatSession = WeChatApiClient.getSessionByCode(loginRequest.getCode());
            System.out.println("[微信登录] 微信API调用成功，openid: " + weChatSession.getOpenid());

            // 验证 openid 是否为空
            if (weChatSession.getOpenid() == null || weChatSession.getOpenid().trim().isEmpty()) {
                System.out.println("[微信登录] 错误: openid为空");
                WeChatLoginResponse errorResponse = new WeChatLoginResponse(
                        false, "获取用户信息失败", null
                );
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(errorResponse));
                return;
            }

            // 5. 保存或更新用户信息到数据库
            System.out.println("[微信登录] 开始处理用户数据...");
            UserDAO userDAO = new UserDAO();
            // 使用openid查找用户
            User existingUser = userDAO.findByOpenId(weChatSession.getOpenid());
            
            if (existingUser == null) {
                // 新用户，插入数据库，code字段初始化为空
                User newUser = new User(
                    weChatSession.getOpenid(), // 使用openid作为用户标识
                    loginRequest.getNickName(),
                    loginRequest.getAvatarUrl()
                );
                // 新用户code字段为空，等待生成邀请码时再填充
                newUser.setCode(null);
                long userId = userDAO.insert(newUser);
                if (userId <= 0) {
                    System.out.println("[微信登录] 错误: 创建新用户失败");
                    WeChatLoginResponse errorResponse = new WeChatLoginResponse(
                            false, "创建用户失败", null
                    );
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(errorResponse));
                    return;
                }
                System.out.println("[微信登录] 新用户创建成功，ID: " + userId);
                existingUser = newUser;
            } else {
                // 老用户，更新信息
                existingUser.setNickName(loginRequest.getNickName());
                existingUser.setAvatarUrl(loginRequest.getAvatarUrl());
                userDAO.update(existingUser);
                System.out.println("[微信登录] 用户信息已更新，ID: " + existingUser.getId());
            }

            // 6. 生成JWT token
            System.out.println("[微信登录] 开始生成JWT token...");
            String token = JwtUtil.generateToken(weChatSession.getOpenid(), weChatSession.getSession_key());
            System.out.println("[微信登录] JWT token生成成功");

            // 7. 构建响应数据
            WeChatLoginResponse.LoginData loginData = new WeChatLoginResponse.LoginData(
                    token,
                    weChatSession.getOpenid(),
                    weChatSession.getSession_key()
            );

            WeChatLoginResponse successResponse = new WeChatLoginResponse(
                    true,
                    "登录成功",
                    loginData
            );

            // 8. 返回响应
            System.out.println("[微信登录] 登录成功，返回响应");
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(successResponse));

        } catch (Exception e) {
            // 异常处理
            System.err.println("[微信登录] 发生异常: " + e.getClass().getName());
            System.err.println("[微信登录] 异常信息: " + e.getMessage());
            e.printStackTrace();
            
            WeChatLoginResponse errorResponse = new WeChatLoginResponse(
                    false,
                    "登录失败: " + e.getMessage(),
                    null
            );
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(errorResponse));
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        WeChatLoginResponse errorResponse = new WeChatLoginResponse(
                false,
                "请使用POST方法访问",
                null
        );
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(errorResponse));
        out.flush();
        out.close();
    }
}