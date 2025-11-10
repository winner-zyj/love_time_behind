package com.abc.love_time.util;

import com.abc.love_time.dao.UserDAO;
import com.abc.love_time.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("开始测试数据库连接...");
        
        // 测试数据库连接
        boolean isConnected = DBUtil.testConnection();
        System.out.println("数据库连接状态: " + (isConnected ? "成功" : "失败"));
        
        if (isConnected) {
            // 测试查询用户数据
            testQueryUsers();
        }
    }
    
    private static void testQueryUsers() {
        System.out.println("\n开始查询用户数据...");
        
        try (Connection conn = DBUtil.getConnection()) {
            // 查询用户表结构
            System.out.println("查询users表结构:");
            PreparedStatement pstmt = conn.prepareStatement("DESCRIBE users");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("  " + rs.getString("Field") + " - " + rs.getString("Type"));
            }
            
            // 查询用户数据
            System.out.println("\n查询用户数据:");
            pstmt = conn.prepareStatement("SELECT * FROM users LIMIT 5");
            rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("  用户ID: " + rs.getLong("id") + 
                                 ", OpenID: " + rs.getString("openid") + 
                                 ", 昵称: " + rs.getString("nickName"));
            }
            System.out.println("总共查询到 " + count + " 个用户");
            
            if (count == 0) {
                System.out.println("用户表为空，请检查是否已正确初始化数据");
            }
            
        } catch (SQLException e) {
            System.err.println("查询用户数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}