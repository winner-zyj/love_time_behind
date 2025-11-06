package com.abc.love_time.util;

import com.abc.love_time.dao.FutureLetterDAO;
import com.abc.love_time.dao.CoupleRelationshipDAO;
import com.abc.love_time.entity.FutureLetter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 未来情书定时发送调度器
 */
public class FutureLetterScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final FutureLetterDAO futureLetterDAO = new FutureLetterDAO();
    private static final CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO();
    
    // 每分钟检查一次是否有需要发送的情书
    private static final long CHECK_INTERVAL = 1; // 分钟
    
    /**
     * 启动定时任务
     */
    public static void start() {
        System.out.println("[FutureLetterScheduler] 启动未来情书定时发送任务");
        
        // 立即执行一次，然后按固定间隔执行
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndSendLetters();
            } catch (Exception e) {
                System.err.println("[FutureLetterScheduler] 检查并发送未来情书时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, CHECK_INTERVAL, TimeUnit.MINUTES);
    }
    
    /**
     * 停止定时任务
     */
    public static void stop() {
        System.out.println("[FutureLetterScheduler] 停止未来情书定时发送任务");
        scheduler.shutdown();
    }
    
    /**
     * 检查并发送到期的未来情书
     */
    private static void checkAndSendLetters() {
        System.out.println("[FutureLetterScheduler] 开始检查需要发送的未来情书");
        
        try {
            // 获取需要发送的未来情书列表
            List<FutureLetter> lettersToSend = futureLetterDAO.findLettersToSend();
            
            if (lettersToSend.isEmpty()) {
                System.out.println("[FutureLetterScheduler] 没有需要发送的未来情书");
                return;
            }
            
            System.out.println("[FutureLetterScheduler] 发现 " + lettersToSend.size() + " 封需要发送的未来情书");
            
            // 发送每封情书
            for (FutureLetter letter : lettersToSend) {
                sendLetter(letter);
            }
        } catch (Exception e) {
            System.err.println("[FutureLetterScheduler] 检查需要发送的未来情书时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 发送单封未来情书
     */
    private static void sendLetter(FutureLetter letter) {
        try {
            System.out.println("[FutureLetterScheduler] 准备发送未来情书，ID: " + letter.getId());
            
            // 更新情书状态为已发送
            Timestamp sentAt = Timestamp.valueOf(LocalDateTime.now());
            boolean updated = futureLetterDAO.updateStatus(letter.getId(), "SENT", sentAt, null);
            
            if (updated) {
                System.out.println("[FutureLetterScheduler] 成功发送未来情书，ID: " + letter.getId());
                
                // 只处理发送给情侣对方的情况
                if ("PARTNER".equals(letter.getDeliveryMethod())) {
                    sendToPartner(letter);
                } else {
                    System.out.println("[FutureLetterScheduler] 情书ID: " + letter.getId() + " 不是发送给情侣对方的类型");
                }
            } else {
                System.err.println("[FutureLetterScheduler] 更新未来情书状态失败，ID: " + letter.getId());
            }
        } catch (Exception e) {
            System.err.println("[FutureLetterScheduler] 发送未来情书时发生错误，ID: " + letter.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 发送给情侣对方
     */
    private static void sendToPartner(FutureLetter letter) {
        System.out.println("[FutureLetterScheduler] 发送给情侣对方，情书ID: " + letter.getId());
        
        try {
            // 获取发送者的情侣关系，确定对方ID
            Long partnerId = coupleDAO.getPartnerId(letter.getSenderId());
            
            if (partnerId != null) {
                System.out.println("[FutureLetterScheduler] 情书ID: " + letter.getId() + " 已发送给情侣对方，对方ID: " + partnerId);
                
                // 这里可以实现具体的推送逻辑，比如：
                // 1. 通过WebSocket推送通知给对方
                // 2. 在数据库中创建一条通知记录
                // 3. 通过移动推送服务（如Firebase、极光推送等）发送通知
                
                // 目前只是打印日志，表示情书已经可以显示在对方界面上
                System.out.println("[FutureLetterScheduler] 情书ID: " + letter.getId() + " 已准备显示在对方用户ID " + partnerId + " 的界面上");
            } else {
                System.err.println("[FutureLetterScheduler] 未找到发送者的情侣关系，情书ID: " + letter.getId());
            }
        } catch (Exception e) {
            System.err.println("[FutureLetterScheduler] 发送给情侣对方时发生错误，情书ID: " + letter.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}