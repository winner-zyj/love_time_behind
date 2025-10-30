# 前端对接文档 - 甜蜜问答API

## 📋 概述

本文档说明前端如何对接甜蜜问答功能的后端API。已按照前端提供的接口规范实现。

---

## 🔧 基础配置

### API 基础路径
```javascript
const BASE_URL = 'http://localhost:8080/lovetime';
```

### 认证方式
所有请求需要在请求头中携带 JWT Token：
```javascript
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

**临时方案**：开发阶段可以通过URL参数传递 `userId`：
```
/api/qna/questions?userId=1
```

---

## 📡 API 接口列表

### 1. 获取问题列表

**前端调用：**
```javascript
export function getQuestions() {
  return http.get(config.API.QNA.LIST);
}
```

**后端接口：**
- **地址：** `GET /api/qna/questions?userId={userId}`
- **请求头：** `Authorization: Bearer {token}`
- **响应示例：**
```json
{
  "success": true,
  "data": {
    "defaultQuestions": [
      {
        "id": 1,
        "text": "我们第一次约会的地点是哪里？",
        "isDefault": true
      },
      {
        "id": 2,
        "text": "你最喜欢我做的哪道菜？",
        "isDefault": true
      }
    ],
    "customQuestions": [
      {
        "id": 101,
        "text": "你最喜欢的电影是什么？",
        "isDefault": false,
        "userId": "1"
      }
    ]
  }
}
```

---

### 2. 提交答案

**前端调用：**
```javascript
export function submitAnswer(answerData) {
  return http.post(config.API.QNA.SUBMIT, {
    questionId: answerData.questionId,
    answer: answerData.answer,
    questionText: answerData.questionText
  });
}
```

**后端接口：**
- **地址：** `POST /api/qna/answer/submit?userId={userId}`
- **请求头：** `Authorization: Bearer {token}`
- **请求体：**
```json
{
  "questionId": 1,
  "answer": "咖啡厅",
  "questionText": "我们第一次约会的地点是哪里？"
}
```

- **响应示例：**
```json
{
  "success": true,
  "message": "提交成功",
  "data": {
    "answerId": 1,
    "partnerAnswer": null,
    "hasPartnerAnswered": false
  }
}
```

**说明：**
- `partnerAnswer` 和 `hasPartnerAnswered` 需要情侣关系表支持，当前返回 null/false
- 重复提交同一问题会自动更新答案

---

### 3. 获取历史回答记录

**前端调用：**
```javascript
export function getHistory(params = {}) {
  return http.get(config.API.QNA.HISTORY, params);
}
```

**后端接口：**
- **地址：** `GET /api/qna/history?userId={userId}&page=1&pageSize=20`
- **请求头：** `Authorization: Bearer {token}`
- **请求参数：**
  - `page`: 页码（可选，默认1）
  - `pageSize`: 每页数量（可选，默认20）

- **响应示例：**
```json
{
  "success": true,
  "data": {
    "total": 15,
    "list": [
      {
        "id": 1,
        "questionId": 1,
        "question": "我们第一次约会的地点是哪里？",
        "myAnswer": "咖啡厅",
        "partnerAnswer": null,
        "createdAt": "2025-10-29 14:30:00.0",
        "updatedAt": "2025-10-29 14:30:00.0"
      }
    ]
  }
}
```

---

### 4. 获取对方答案

**前端调用：**
```javascript
export function getPartnerAnswer(questionId) {
  return http.get(config.API.QNA.PARTNER_ANSWER, { questionId });
}
```

**后端接口：**
- **地址：** `GET /api/qna/partner?userId={userId}&questionId={questionId}`
- **请求头：** `Authorization: Bearer {token}`
- **响应示例：**
```json
{
  "success": true,
  "data": {
    "hasAnswered": false,
    "answer": null,
    "answeredAt": null
  }
}
```

**说明：** 当前返回空数据，需要实现情侣关系表后才能返回真实数据

---

### 5. 添加自定义问题

**前端调用：**
```javascript
export function addCustomQuestion(questionText) {
  return http.post(config.API.QNA.ADD_QUESTION, {
    text: questionText
  });
}
```

**后端接口：**
- **地址：** `POST /api/qna/question/add?userId={userId}`
- **请求头：** `Authorization: Bearer {token}`
- **请求体：**
```json
{
  "text": "你最喜欢的电影是什么？"
}
```

- **响应示例：**
```json
{
  "success": true,
  "message": "添加成功",
  "data": {
    "id": 6,
    "text": "你最喜欢的电影是什么？",
    "isDefault": false,
    "userId": "1",
    "createdAt": "Wed Oct 29 15:30:00 CST 2025"
  }
}
```

---

### 6. 删除自定义问题

**前端调用：**
```javascript
export function deleteCustomQuestion(questionId) {
  return http.post(config.API.QNA.DELETE_QUESTION, {
    questionId
  });
}
```

**后端接口：**
- **地址：** `POST /api/qna/question/delete?userId={userId}`
- **请求头：** `Authorization: Bearer {token}`
- **请求体：**
```json
{
  "questionId": 6
}
```

- **响应示例：**
```json
{
  "success": true,
  "message": "删除成功"
}
```

**限制：**
- 只能删除自己创建的自定义问题
- 不能删除预设问题

---

## 🔐 认证流程

### 开发阶段（临时方案）

由于JWT集成需要时间，当前支持两种方式传递用户ID：

**方式1：通过URL参数（推荐）**
```javascript
const userId = 1;
const url = `/api/qna/questions?userId=${userId}`;
```

**方式2：通过Authorization头**
```javascript
headers: {
  'Authorization': 'Bearer your-token-here'
}
// 同时需要在URL中传递userId参数
```

### 生产环境

后续需要实现完整的JWT认证：
1. 用户登录后获取JWT token
2. 将token存储在localStorage或cookie中
3. 每次请求都携带token
4. 后端从token中解析用户ID

---

## ⚠️ 注意事项

### 1. 字段名称对应

| 前端字段 | 后端字段 | 说明 |
|---------|---------|------|
| `defaultQuestions` | `presetQuestions` | 预设问题列表 |
| `text` | `questionText` | 问题文本 |
| `myAnswer` | `answerText` | 我的答案 |

后端已适配前端字段名称。

### 2. 情侣关系功能

以下字段需要情侣关系表支持（当前返回null/false）：
- `partnerAnswer` - 对方的答案
- `hasPartnerAnswered` - 对方是否已回答

**建议后续实现：**
```sql
CREATE TABLE couples (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    status ENUM('pending', 'active') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id),
    FOREIGN KEY (user2_id) REFERENCES users(id),
    UNIQUE KEY unique_couple (user1_id, user2_id)
);
```

### 3. 分页支持

历史记录接口已支持分页，前端可传递：
- `page`: 页码（默认1）
- `pageSize`: 每页数量（默认20）

### 4. 错误处理

所有接口错误响应格式：
```json
{
  "success": false,
  "message": "错误信息描述"
}
```

常见HTTP状态码：
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未认证
- `403` - 无权限
- `404` - 资源不存在
- `500` - 服务器错误

---

## 🧪 测试示例

### 使用 curl 测试

```bash
# 1. 获取问题列表
curl "http://localhost:8080/lovetime/api/qna/questions?userId=1"

# 2. 提交答案
curl -X POST "http://localhost:8080/lovetime/api/qna/answer/submit?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"questionId":1,"answer":"咖啡厅","questionText":"我们第一次约会的地点是哪里？"}'

# 3. 获取历史记录
curl "http://localhost:8080/lovetime/api/qna/history?userId=1&page=1&pageSize=10"

# 4. 添加自定义问题
curl -X POST "http://localhost:8080/lovetime/api/qna/question/add?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"text":"你最喜欢的电影是什么？"}'

# 5. 删除自定义问题
curl -X POST "http://localhost:8080/lovetime/api/qna/question/delete?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"questionId":6}'
```

---

## 📦 配置文件示例

### config.js
```javascript
export default {
  API: {
    QNA: {
      LIST: '/api/qna/questions',
      SUBMIT: '/api/qna/answer/submit',
      HISTORY: '/api/qna/history',
      PARTNER_ANSWER: '/api/qna/partner',
      ADD_QUESTION: '/api/qna/question/add',
      DELETE_QUESTION: '/api/qna/question/delete'
    }
  }
}
```

---

## 🚀 部署清单

### 后端部署步骤

1. ✅ 数据库已初始化（sweet_qa_schema.sql）
2. ✅ QnaServlet 已创建并注册
3. ✅ 重新构建项目（Build → Rebuild Project）
4. ✅ 重启 Tomcat 服务器
5. ✅ 验证接口可访问性

### 前端配置步骤

1. 确认 `config.js` 中的 API 路径配置
2. 确认 `http.js` 中的请求拦截器配置
3. 测试所有接口调用
4. 处理错误响应

---

## 📞 技术支持

如遇问题，请检查：
1. Tomcat 控制台日志
2. 浏览器控制台网络请求
3. 数据库表数据
4. Authorization token 是否正确

**日志位置：** Tomcat 控制台会输出详细的请求日志，格式如：
```
[QnaServlet] GET请求路径: /questions
[QnaServlet] 获取用户 1 的问题列表
[QnaServlet] 返回 5 个预设问题，1 个自定义问题
```
