# 甜蜜问答功能 API 文档

## 基础信息

- **基础路径**: `http://localhost:8080/lovetime`
- **请求头**: `Content-Type: application/json; charset=UTF-8`
- **认证**: 使用 JWT Token（如需要，在请求头添加 `Authorization: Bearer {token}`）

---

## API 接口列表

### 1. 获取问题列表

**请求**
```
GET /api/questions/list?userId={userId}
```

**参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**
```json
{
  "success": true,
  "data": {
    "presetQuestions": [
      {
        "id": 1,
        "questionText": "我们第一次约会的地点是哪里？",
        "category": "preset",
        "orderIndex": 1,
        "hasAnswered": false,
        "userAnswer": null
      }
    ],
    "customQuestions": [
      {
        "id": 6,
        "questionText": "你最喜欢的电影是什么？",
        "category": "custom",
        "orderIndex": 0,
        "hasAnswered": true,
        "userAnswer": "盗梦空间"
      }
    ],
    "totalCount": 6,
    "completedCount": 2,
    "currentQuestionId": 3
  }
}
```

---

### 2. 获取单个问题详情

**请求**
```
GET /api/questions/{questionId}
```

**路径参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| questionId | Long | 是 | 问题ID |

**响应示例**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "questionText": "我们第一次约会的地点是哪里？",
    "category": "preset",
    "orderIndex": 1,
    "hasAnswered": null,
    "userAnswer": null
  }
}
```

---

### 3. 获取下一题

**请求**
```
GET /api/questions/next?userId={userId}&currentQuestionId={currentQuestionId}
```

**参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| currentQuestionId | Long | 否 | 当前问题ID（不传则返回第一题） |

**响应示例（有下一题）**
```json
{
  "success": true,
  "data": {
    "hasNext": true,
    "question": {
      "id": 2,
      "questionText": "你最喜欢我做的哪道菜？",
      "category": "preset",
      "orderIndex": 2
    }
  }
}
```

**响应示例（无下一题）**
```json
{
  "success": true,
  "data": {
    "hasNext": false,
    "message": "已完成所有问题"
  }
}
```

---

### 4. 提交答案

**请求**
```
POST /api/answers/submit
Content-Type: application/json

{
  "userId": 123,
  "questionId": 1,
  "answerText": "咖啡厅"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| questionId | Long | 是 | 问题ID |
| answerText | String | 是 | 答案内容 |

**响应示例（有下一题）**
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

**响应示例（无下一题）**
```json
{
  "success": true,
  "data": {
    "answerId": 5,
    "message": "答案提交成功",
    "hasNext": false
  }
}
```

---

### 5. 添加自定义问题

**请求**
```
POST /api/questions/custom
Content-Type: application/json

{
  "userId": 123,
  "questionText": "你最喜欢的电影是什么？"
}
```

**请求参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| questionText | String | 是 | 问题内容 |

**响应示例**
```json
{
  "success": true,
  "data": {
    "questionId": 6,
    "message": "自定义问题添加成功"
  }
}
```

---

### 6. 删除自定义问题

**请求**
```
DELETE /api/questions/{questionId}
```

**路径参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| questionId | Long | 是 | 问题ID（仅能删除自定义问题） |

**响应示例**
```json
{
  "success": true,
  "data": {
    "message": "问题删除成功"
  }
}
```

**错误响应**
```json
{
  "success": false,
  "message": "只能删除自定义问题"
}
```

---

### 7. 查看历史答案

**请求**
```
GET /api/answers/history?userId={userId}
```

**参数**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**
```json
{
  "success": true,
  "data": {
    "total": 3,
    "history": [
      {
        "answerId": 1,
        "questionId": 1,
        "questionText": "我们第一次约会的地点是哪里？",
        "answerText": "咖啡厅",
        "answeredAt": "2025-10-29 14:30:00",
        "category": "preset"
      },
      {
        "answerId": 2,
        "questionId": 2,
        "questionText": "你最喜欢我做的哪道菜？",
        "answerText": "红烧肉",
        "answeredAt": "2025-10-29 14:35:00",
        "category": "preset"
      }
    ]
  }
}
```

---

## 错误响应格式

所有错误响应遵循以下格式：

```json
{
  "success": false,
  "message": "错误信息描述"
}
```

**常见错误码**
- `400 Bad Request` - 请求参数错误
- `404 Not Found` - 资源不存在
- `403 Forbidden` - 禁止操作（如删除预设问题）
- `500 Internal Server Error` - 服务器内部错误

---

## 前端集成示例

### 微信小程序示例

```javascript
// 获取问题列表
async function getQuestionList(userId) {
  const res = await wx.request({
    url: 'http://localhost:8080/lovetime/api/questions/list',
    method: 'GET',
    data: { userId },
    header: {
      'Content-Type': 'application/json'
    }
  });
  
  if (res.data.success) {
    return res.data.data;
  } else {
    wx.showToast({ title: res.data.message, icon: 'none' });
  }
}

// 提交答案
async function submitAnswer(userId, questionId, answerText) {
  const res = await wx.request({
    url: 'http://localhost:8080/lovetime/api/answers/submit',
    method: 'POST',
    data: { userId, questionId, answerText },
    header: {
      'Content-Type': 'application/json'
    }
  });
  
  if (res.data.success) {
    const { hasNext, nextQuestion } = res.data.data;
    if (hasNext) {
      // 跳转到下一题
      console.log('下一题:', nextQuestion);
    } else {
      wx.showToast({ title: '已完成所有问题！', icon: 'success' });
    }
  }
}

// 添加自定义问题
async function addCustomQuestion(userId, questionText) {
  const res = await wx.request({
    url: 'http://localhost:8080/lovetime/api/questions/custom',
    method: 'POST',
    data: { userId, questionText },
    header: {
      'Content-Type': 'application/json'
    }
  });
  
  if (res.data.success) {
    wx.showToast({ title: '问题添加成功', icon: 'success' });
  }
}
```

---

## 数据库表结构

### questions（问题表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 问题ID（主键） |
| question_text | VARCHAR(500) | 问题内容 |
| category | ENUM('preset', 'custom') | 问题类型 |
| created_by | BIGINT | 创建者ID |
| is_active | BOOLEAN | 是否启用 |
| order_index | INT | 排序序号 |

### answers（答案表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 答案ID（主键） |
| question_id | BIGINT | 问题ID |
| user_id | BIGINT | 用户ID |
| answer_text | TEXT | 答案内容 |
| answered_at | TIMESTAMP | 回答时间 |

### user_question_progress（答题进度表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 进度ID（主键） |
| user_id | BIGINT | 用户ID |
| current_question_id | BIGINT | 当前问题ID |
| completed_count | INT | 已完成数 |
| total_count | INT | 总问题数 |

---

## 注意事项

1. **编码问题**：所有请求和响应都使用 UTF-8 编码
2. **唯一性约束**：同一用户对同一问题只能有一个答案（重复提交会更新答案）
3. **问题类型**：预设问题（preset）不能删除，只能删除自定义问题（custom）
4. **进度跟踪**：系统自动跟踪用户答题进度，无需手动维护
5. **数据验证**：后端会验证所有必填参数，确保数据完整性

---

## 部署说明

### 启动项目
1. 确保数据库已执行 `sweet_qa_schema.sql` 初始化脚本
2. 在 IntelliJ IDEA 中启动 Tomcat 服务器
3. 访问 `http://localhost:8080/lovetime` 确认服务运行

### 测试接口
使用 Postman 或 curl 测试接口：

```bash
# 测试获取问题列表
curl "http://localhost:8080/lovetime/api/questions/list?userId=1"

# 测试提交答案
curl -X POST "http://localhost:8080/lovetime/api/answers/submit" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"questionId":1,"answerText":"咖啡厅"}'
```
