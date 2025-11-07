# API 路径对照表

## 前端与后端接口映射关系

| 功能 | 前端期望路径 | 后端实现路径 | Servlet | 状态 |
|------|------------|------------|---------|------|
| **获取问题列表** | `/api/qna/questions` | `/api/qna/questions` | QnaServlet | ✅ 已实现 |
| **提交答案** | `/api/qna/answer/submit` | `/api/qna/answer/submit` | QnaServlet | ✅ 已实现 |
| **历史记录** | `/api/qna/history` | `/api/qna/history` | QnaServlet | ✅ 已实现（支持分页）|
| **对方答案** | `/api/qna/partner` | `/api/qna/partner` | QnaServlet | ⚠️ 已实现（返回空数据）|
| **添加问题** | `/api/qna/question/add` | `/api/qna/question/add` | QnaServlet | ✅ 已实现 |
| **删除问题** | `/api/qna/question/delete` | `/api/qna/question/delete` | QnaServlet | ✅ 已实现 |
| **获取相爱天数** | `/api/couple/love-days` | `/api/couple/love-days` | CoupleServlet | ✅ 已实现 |

---

## 兼容性说明

### ✅ 完全兼容的接口

1. **获取问题列表**
   - 字段映射：`defaultQuestions` ↔ 预设问题，`customQuestions` ↔ 自定义问题
   - 返回格式：完全符合前端要求

2. **提交答案**
   - 请求参数：`questionId`, `answer`, `questionText`
   - 返回格式：包含 `answerId`, `partnerAnswer`, `hasPartnerAnswered`

3. **添加自定义问题**
   - 请求参数：`text`
   - 返回格式：包含 `id`, `text`, `isDefault`, `userId`, `createdAt`

4. **删除自定义问题**
   - 请求参数：`questionId`
   - 权限验证：只能删除自己创建的问题

5. **获取相爱天数**
   - 请求参数：无
   - 返回格式：包含 `loveDays`, `anniversaryDate`, `relationshipName`

### ⚠️ 部分功能待实现

1. **获取对方答案**
   - 当前状态：返回空数据（`hasAnswered: false`, `answer: null`）
   - 原因：需要情侣关系表支持
   - 计划：后续版本实现

2. **历史记录中的对方答案**
   - 当前状态：`partnerAnswer` 字段返回 `null`
   - 原因：需要情侣关系表支持
   - 计划：后续版本实现

---

## 认证方式

### 开发阶段（当前）
```
方式1：URL参数
GET /api/qna/questions?userId=1

方式2：Authorization头 + URL参数
Headers: Authorization: Bearer {token}
URL: /api/qna/questions?userId=1
```

### 生产环境（规划）
```
仅使用 Authorization 头
Headers: Authorization: Bearer {token}
后端从JWT中解析userId
```

---

## 数据格式对照

### 问题对象
```javascript
// 前端期望格式
{
  id: number,
  text: string,          // 后端字段: questionText
  isDefault: boolean     // 后端字段: category === 'preset'
}

// 后端数据库字段
{
  id: BIGINT,
  question_text: VARCHAR(500),
  category: ENUM('preset', 'custom')
}
```

### 答案对象
```javascript
// 前端期望格式
{
  questionId: number,
  answer: string,        // 后端字段: answerText
  questionText: string   // 可选字段，仅用于记录
}

// 后端数据库字段
{
  question_id: BIGINT,
  answer_text: TEXT,
  user_id: BIGINT
}
```

### 相爱天数对象
```javascript
// 前端期望格式
{
  loveDays: number,      // 相爱天数
  anniversaryDate: string, // 纪念日日期 (YYYY-MM-DD)
  relationshipName: string // 关系昵称
}

// 后端数据库字段
{
  love_days: INT,
  anniversary_date: DATE,
  relationship_name: VARCHAR(100)
}
```

---

## 响应格式标准

### 成功响应
```json
{
  "success": true,
  "data": { /* 具体数据 */ }
}
```

### 成功响应（带消息）
```json
{
  "success": true,
  "message": "操作成功",
  "data": { /* 具体数据 */ }
}
```

### 错误响应
```json
{
  "success": false,
  "message": "错误描述"
}
```

---

## HTTP 状态码

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 | 成功 | 所有成功的请求 |
| 400 | 请求参数错误 | 缺少必填参数、参数格式错误 |
| 401 | 未认证 | token缺失或过期 |
| 403 | 无权限 | 尝试删除他人的问题 |
| 404 | 资源不存在 | 问题ID不存在 |
| 500 | 服务器错误 | 数据库连接失败等 |

---

## 测试用例

### 1. 获取问题列表
```bash
curl "http://localhost:8080/lovetime/api/qna/questions?userId=1"
```
**预期结果：** 返回预设问题和自定义问题列表

### 2. 提交答案
```bash
curl -X POST "http://localhost:8080/lovetime/api/qna/answer/submit?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"questionId":1,"answer":"咖啡厅"}'
```
**预期结果：** 返回 `answerId` 和提交成功消息

### 3. 获取历史记录（分页）
```bash
curl "http://localhost:8080/lovetime/api/qna/history?userId=1&page=1&pageSize=10"
```
**预期结果：** 返回分页后的历史记录

### 4. 添加自定义问题
```bash
curl -X POST "http://localhost:8080/lovetime/api/qna/question/add?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"text":"你最喜欢的电影是什么？"}'
```
**预期结果：** 返回新问题的ID和详细信息

### 5. 删除自定义问题
```bash
curl -X POST "http://localhost:8080/lovetime/api/qna/question/delete?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"questionId":6}'
```
**预期结果：** 返回删除成功消息

### 6. 获取相爱天数
```bash
curl "http://localhost:8080/lovetime/api/couple/love-days" \
  -H "Authorization: Bearer {用户token}"
```
**预期结果：** 返回相爱天数信息

---

## 前端集成检查清单

- [ ] 确认 API 基础路径配置正确
- [ ] 确认请求头包含 `Content-Type: application/json`
- [ ] 确认所有请求都传递 `userId` 参数
- [ ] 测试问题列表获取功能
- [ ] 测试答案提交功能
- [ ] 测试历史记录获取功能（含分页）
- [ ] 测试自定义问题添加功能
- [ ] 测试自定义问题删除功能
- [ ] 测试获取相爱天数功能
- [ ] 处理所有错误响应
- [ ] 验证中文内容显示正常

---

## 版本历史

### v1.0.0 (2025-10-29)
- ✅ 实现所有前端要求的API接口
- ✅ 支持获取问题列表
- ✅ 支持提交答案
- ✅ 支持历史记录查询（含分页）
- ✅ 支持添加/删除自定义问题
- ⚠️ 对方答案功能返回空数据（待实现情侣关系表）

### v1.1.0 (2025-11-07)
- ✅ 实现获取相爱天数接口
- ✅ 支持情侣关系确认时间计算
- ✅ 支持纪念日和关系昵称显示

### 计划中功能
- [ ] 实现情侣关系表
- [ ] 实现完整的JWT认证
- [ ] 实现对方答案查询
- [ ] 实现答案匹配度计算
- [ ] 添加问题分类功能