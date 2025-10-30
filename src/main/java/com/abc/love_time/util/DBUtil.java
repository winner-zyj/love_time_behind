package com.abc.love_time.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 */
public class DBUtil {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    // 静态代码块，加载数据库配置
    static {
        try {
            // 加载配置文件
            Properties props = new Properties();
            InputStream input = DBUtil.class.getClassLoader()
                    .getResourceAsStream("database.properties");
            
            if (input == null) {
                System.err.println("无法找到 database.properties 配置文件");
                throw new RuntimeException("数据库配置文件不存在");
            }
            
            props.load(input);
            
            // 读取配置
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driver = props.getProperty("db.driver");
            
            // 加载数据库驱动
            Class.forName(driver);
            
            System.out.println("[数据库] 配置加载成功");
            System.out.println("[数据库] URL: " + url);
            System.out.println("[数据库] 用户名: " + username);
            
        } catch (Exception e) {
            System.err.println("[数据库] 配置加载失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 获取数据库连接
     * @return Connection对象
     * @throws SQLException SQL异常
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("[数据库] 连接成功");
            return conn;
        } catch (SQLException e) {
            System.err.println("[数据库] 连接失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 关闭数据库连接
     * @param conn 数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("[数据库] 连接已关闭");
            } catch (SQLException e) {
                System.err.println("[数据库] 关闭连接失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 测试数据库连接
     * @return 连接是否成功
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[数据库] 连接测试失败: " + e.getMessage());
            return false;
        }
    }
}
