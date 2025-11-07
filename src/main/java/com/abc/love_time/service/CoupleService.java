package com.abc.love_time.service;

import com.abc.love_time.dao.CoupleRelationshipDAO;
import com.abc.love_time.entity.CoupleRelationship;
import com.abc.love_time.dto.LoveDaysResponse;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 情侣关系服务类
 */
public class CoupleService {
    private final CoupleRelationshipDAO coupleDAO = new CoupleRelationshipDAO();

    /**
     * 计算相爱天数
     * @param userId 用户ID
     * @return 相爱天数响应对象
     */
    public LoveDaysResponse calculateLoveDays(Long userId) {
        try {
            // 获取用户的情侣关系
            CoupleRelationship relationship = coupleDAO.findByUserId(userId);
            
            if (relationship == null) {
                return LoveDaysResponse.error("未找到情侣关系");
            }
            
            if (!"active".equals(relationship.getStatus())) {
                return LoveDaysResponse.error("情侣关系未激活");
            }
            
            // 优先使用确认绑定时间计算相爱天数
            java.sql.Timestamp confirmedAt = relationship.getConfirmedAt();
            java.sql.Timestamp createdAt = relationship.getCreatedAt();
            
            // 如果confirmedAt为空，则使用createdAt
            if (confirmedAt == null) {
                System.out.println("[CoupleService] confirmedAt为空，使用createdAt作为起始时间");
                confirmedAt = createdAt;
            }
            
            // 如果仍然为空，返回错误
            if (confirmedAt == null) {
                return LoveDaysResponse.error("情侣关系时间信息为空");
            }
            
            // 计算天数差
            LocalDate startDate = confirmedAt.toLocalDateTime().toLocalDate();
            LocalDate currentDate = LocalDate.now();
            long loveDays = ChronoUnit.DAYS.between(startDate, currentDate);
            
            // 确保天数不为负数
            if (loveDays < 0) {
                loveDays = 0;
            }
            
            // 创建响应数据
            LoveDaysResponse.LoveDaysData data = new LoveDaysResponse.LoveDaysData();
            data.setLoveDays((int) loveDays);
            data.setAnniversaryDate(relationship.getAnniversaryDate() != null ? 
                relationship.getAnniversaryDate().toString() : null);
            data.setRelationshipName(relationship.getRelationshipName());
            
            System.out.println("[CoupleService] 用户 " + userId + " 相爱天数计算结果: " + loveDays + "天，起始时间: " + startDate);
            
            return LoveDaysResponse.success("获取相爱天数成功", data);
            
        } catch (Exception e) {
            System.err.println("[CoupleService] 计算相爱天数失败: " + e.getMessage());
            e.printStackTrace();
            return LoveDaysResponse.error("计算相爱天数失败: " + e.getMessage());
        }
    }
}