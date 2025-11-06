package com.abc.love_time.servlet;

import com.abc.love_time.util.FutureLetterScheduler;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * 应用启动监听器
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[AppStartupListener] 应用启动，初始化定时任务");
        
        // 启动未来情书定时发送任务
        FutureLetterScheduler.start();
        
        System.out.println("[AppStartupListener] 定时任务初始化完成");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[AppStartupListener] 应用关闭，停止定时任务");
        
        // 停止未来情书定时发送任务
        FutureLetterScheduler.stop();
        
        System.out.println("[AppStartupListener] 定时任务已停止");
    }
}