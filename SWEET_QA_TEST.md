# 甜蜜问答功能 - 快速测试指南

## 📋 测试前准备

### 1. 确认数据库已初始化
```sql
-- 连接到数据库
USE lovetime;

-- 检查表是否创建成功
SHOW TABLES;
-- 应该看到: questions, answers, user_question_progress

-- 检查预设问题是否插入
SELECT * FROM questions WHERE category = 'preset';
-- 应该看到 5 个预设问题
```

### 2. 确认项目已启动
- 在 IntelliJ IDEA 中启动 Tomcat 服务器
- 访问: http://localhost:8080/lovetime
- 确认服务正常运行

---

## 🧪 API 测试步骤

### 测试 1: 获取问题列表

**请求**
```bash
curl "http://localhost:8080/lovetime/api/questions/list?userId=1"
```

**预期结果**
```json
{
  "success": true,
  "data": {
    "presetQuestions": [ /* 5个预设问题 */ ],
    "customQuestions": [],
    "totalCount": 5,
    "completedCount": 0,
    "currentQuestionId": null
  }
}
```

---

### 测试 2: 获取单个问题详情

**请求**
```bash
curl "http://localhost:8080/lovetime/api/questions/1"
```

**预期结果**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "questionText": "我们第一次约会的地点是哪里？",
    "category": "preset",
    "orderIndex": 1
  }
}
```

---

### 测试 3: 提交第一个答案

**请求**
```bash
curl -X POST "http://localhost:8080/lovetime/api/answers/submit" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d "{\"userId\":1,\"questionId\":1,\"answerText\":\"咖啡厅\"}"
```

**预期结果**
```json
{
  "success": true,
  "data": {
    "answerId": 1,
    "message": "答案提交成功",
    "hasNext": true,
    "nextQuestion": {
      "id": 2,
      "questionText": "你最喜欢我做的哪道菜？"
    }
  }
}
```

**验证数据库**
```sql
-- 检查答案是否保存
SELECT * FROM answers WHERE user_id = 1;

-- 检查进度是否更新
SELECT * FROM user_question_progress WHERE user_id = 1;
```

---

### 测试 4: 提交第二个答案

**请求**
```bash
curl -X POST "http://localhost:8080/lovetime/api/answers/submit" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d "{\"userId\":1,\"questionId\":2,\"answerText\":\"红烧肉\"}"
```

---

### 测试 5: 更新已有答案（重复提交）

**请求**
```bash
curl -X POST "http://localhost:8080/lovetime/api/answers/submit" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d "{\"userId\":1,\"questionId\":1,\"answerText\":\"星巴克咖啡厅\"}"
```

**预期结果**
- 答案应该被更新，而不是插入新记录
- `answerId` 保持不变

**验证数据库**
```sql
-- 应该只有1条记录，且答案内容已更新
SELECT * FROM answers WHERE user_id = 1 AND question_id = 1;
```

---

### 测试 6: 获取下一题

**请求**
```bash
curl "http://localhost:8080/lovetime/api/questions/next?userId=1&currentQuestionId=2"
```

**预期结果**
```json
{
  "success": true,
  "data": {
    "hasNext": true,
    "question": {
      "id": 3,
      "questionText": "如果周末只做一件事，你希望是什么？"
    }
  }
}
```

---

### 测试 7: 添加自定义问题

**请求**
```bash
curl -X POST "http://localhost:8080/lovetime/api/questions/custom" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d "{\"userId\":1,\"questionText\":\"你最喜欢的电影是什么？\"}"
```

**预期结果**
```json
{
  "success": true,
  "data": {
    "questionId": 6,
    "message": "自定义问题添加成功"
  }
}
```

**验证数据库**
```sql
SELECT * FROM questions WHERE category = 'custom';
```

---

### 测试 8: 再次获取问题列表（应包含自定义问题）

**请求**
```bash
curl "http://localhost:8080/lovetime/api/questions/list?userId=1"
```

**预期结果**
- `presetQuestions`: 5个
- `customQuestions`: 1个
- `totalCount`: 6
- `completedCount`: 2

---

### 测试 9: 查看历史答案

**请求**
```bash
curl "http://localhost:8080/lovetime/api/answers/history?userId=1"
```

**预期结果**
```json
{
  "success": true,
  "data": {
    "total": 2,
    "history": [
      {
        "answerId": 2,
        "questionId": 2,
        "questionText": "你最喜欢我做的哪道菜？",
        "answerText": "红烧肉",
        "answeredAt": "2025-10-29 ...",
        "category": "preset"
      },
      {
        "answerId": 1,
        "questionId": 1,
        "questionText": "我们第一次约会的地点是哪里？",
        "answerText": "星巴克咖啡厅",
        "answeredAt": "2025-10-29 ...",
        "category": "preset"
      }
    ]
  }
}
```

---

### 测试 10: 删除自定义问题

**请求**
```bash
curl -X DELETE "http://localhost:8080/lovetime/api/questions/6"
```

**预期结果**
```json
{
  "success": true,
  "data": {
    "message": "问题删除成功"
  }
}
```

---

### 测试 11: 尝试删除预设问题（应失败）

**请求**
```bash
curl -X DELETE "http://localhost:8080/lovetime/api/questions/1"
```

**预期结果**
```json
{
  "success": false,
  "message": "只能删除自定义问题"
}
```

---

## 🔍 常见问题排查

### 问题1: 404 错误
**原因**: Servlet 路径配置错误或服务未启动  
**解决**:
1. 检查 Tomcat 是否正常启动
2. 确认 context path 设置为 `/lovetime`
3. 检查 URL 是否正确

### 问题2: 500 错误
**原因**: 数据库连接失败或代码异常  
**解决**:
1. 检查 `database.properties` 配置
2. 查看 Tomcat 控制台日志
3. 检查数据库表是否存在

### 问题3: 中文乱码
**原因**: 字符编码设置错误  
**解决**:
1. 确保数据库使用 utf8mb4 字符集
2. 检查 Servlet 响应头设置 `UTF-8`
3. curl 请求时指定 `charset=UTF-8`

### 问题4: 数据未保存
**原因**: DAO 层异常或事务回滚  
**解决**:
1. 查看控制台错误日志
2. 检查数据库表结构是否正确
3. 验证外键约束是否满足

---

## 📊 数据库验证 SQL

```sql
-- 1. 查看所有问题
SELECT id, question_text, category, created_by, is_active 
FROM questions 
ORDER BY category, order_index;

-- 2. 查看某用户的所有答案
SELECT a.id, q.question_text, a.answer_text, a.answered_at
FROM answers a
JOIN questions q ON a.question_id = q.id
WHERE a.user_id = 1
ORDER BY a.answered_at DESC;

-- 3. 查看用户进度
SELECT 
    u.nickName,
    p.completed_count,
    p.total_count,
    q.question_text as current_question
FROM user_question_progress p
JOIN users u ON p.user_id = u.id
LEFT JOIN questions q ON p.current_question_id = q.id
WHERE p.user_id = 1;

-- 4. 统计数据
SELECT 
    (SELECT COUNT(*) FROM questions WHERE is_active = TRUE) as total_questions,
    (SELECT COUNT(*) FROM questions WHERE category = 'preset') as preset_count,
    (SELECT COUNT(*) FROM questions WHERE category = 'custom') as custom_count,
    (SELECT COUNT(*) FROM answers) as total_answers;
```

---

## ✅ 完整测试检查清单

- [ ] 数据库表创建成功（questions, answers, user_question_progress）
- [ ] 5个预设问题已插入
- [ ] 获取问题列表接口正常
- [ ] 获取单个问题详情正常
- [ ] 提交答案接口正常
- [ ] 答案自动更新（重复提交同一问题）
- [ ] 获取下一题接口正常
- [ ] 添加自定义问题正常
- [ ] 删除自定义问题正常
- [ ] 不能删除预设问题（权限验证）
- [ ] 查看历史答案正常
- [ ] 用户进度自动更新
- [ ] 中文内容正常显示（无乱码）

---

## 🎯 下一步

测试通过后，前端可以开始对接：
1. 参考 `SWEET_QA_API.md` 文档
2. 使用提供的微信小程序集成示例
3. 根据实际需求调整界面交互流程

如有问题，查看 Tomcat 控制台日志或数据库日志进行调试。
