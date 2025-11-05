# 前端对接文档 - 一百事挑战API

## 📋 概述

本文档说明前端如何对接一百事挑战功能的后端API。系统支持预设12个任务，用户可自定义扩展到100件。

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

---

## 📡 API 接口列表

### 1. 获取任务列表

**前端调用：**
```javascript
export function getTasks() {
  return http.get('/api/challenge/tasks');
}
```

**后端接口：**
- **地址：** `GET /api/challenge/tasks`
- **请求头：** `Authorization: Bearer {token}`
- **响应示例：**
```json
{
  "success": true,
  "message": "获取成功",
  "tasks": [
    {
      "id": 1,
      "taskName": "一起看日出",
      "taskDescription": "找一个美好的清晨，一起迎接第一缕阳光",
      "taskIndex": 1,
      "category": "preset",
      "iconUrl": null,
      "status": "pending",
      "photoUrl": null,
      "note": null,
      "isFavorited": false,
      "completedAt": null
    },
    {
      "id": 13,
      "taskName": "一起去旅行",
      "taskDescription": "去一个从未去过的地方",
      "taskIndex": null,
      "category": "custom",
      "iconUrl": null,
      "status": "completed",
      "photoUrl": "https://example.com/photo.jpg",
      "note": "去了云南，很美！",
      "isFavorited": true,
      "completedAt": "2025-10-25T15:30:00"
    }
  ]
}
```

**字段说明：**
- `id`: 任务ID
- `taskName`: 任务名称
- `taskDescription`: 任务描述
- `taskIndex`: 排序序号（预设任务1-12，自定义任务为null）
- `category`: 任务类型
  - `preset`: 预设任务（系统自带12个）
  - `custom`: 自定义任务（用户添加）
- `status`: 完成状态
  - `pending`: 未完成
  - `completed`: 已完成
- `photoUrl`: 完成任务时上传的照片URL
- `note`: 完成任务时的备注说明
- `isFavorited`: 是否收藏（true/false）
- `completedAt`: 完成时间（ISO 8601格式）

---

### 2. 获取用户进度

**前端调用：**
```javascript
export function getProgress() {
  return http.get('/api/challenge/progress');
}
```

**后端接口：**
- **地址：** `GET /api/challenge/progress`
- **请求头：** `Authorization: Bearer {token}`
- **响应示例：**
```json
{
  "success": true,
  "message": "获取成功",
  "progress": {
    "totalTasks": 15,
    "completedCount": 8,
    "favoritedCount": 5,
    "completionRate": 53.33,
    "lastActiveAt": "2025-10-31T10:00:00"
  }
}
```

**字段说明：**
- `totalTasks`: 总任务数（预设12 + 用户自定义）
- `completedCount`: 已完成任务数
- `favoritedCount`: 收藏任务数
- `completionRate`: 完成率（百分比，保留2位小数）
- `lastActiveAt`: 最后活跃时间

---

### 3. 添加自定义任务

**前端调用：**
```javascript
export function addTask(taskData) {
  return http.post('/api/challenge/task/add', {
    taskName: taskData.taskName,
    taskDescription: taskData.taskDescription
  });
}
```

**后端接口：**
- **地址：** `POST /api/challenge/task/add`
- **请求头：** `Authorization: Bearer {token}`
- **请求体：**
```json
{
  "taskName": "一起去旅行",
  "taskDescription": "去一个从未去过的地方"
}
```

**参数说明：**
- `taskName` (必填): 任务名称，不能为空
- `taskDescription` (可选): 任务描述

**成功响应：**
```json
{
  "success": true,
  "message": "任务添加成功",
  "task": {
    "id": 13,
    "taskName": "一起去旅行",
    "taskDescription": "去一个从未去过的地方",
    "taskIndex": null,
    "category": "custom",
    "createdBy": 4,
    "iconUrl": null,
    "isActive": true,
    "createdAt": "2025-10-31T15:30:00",
    "updatedAt": "2025-10-31T15:30:00"
  }
}
```

**错误响应：**
```json
{
  "success": false,
  "message": "任务名称不能为空"
}
```

---

### 4. 删除自定义任务

**前端调用：**
```javascript
export function deleteTask(taskId) {
  return http.post('/api/challenge/task/delete', {
    taskId: taskId
  });
}
```

**后端接口：**
- **地址：** `POST /api/challenge/task/delete`
- **请求头：** `Authorization: Bearer {token}`
- **请求体：**
```json
{
  "taskId": 13
}
```

**参数说明：**
- `taskId` (必填): 要删除的任务ID

**成功响应：**
```json
{
  "success": true,
  "message": "任务删除成功"
}
```

**错误响应：**
```json
{
  "success": false,
  "message": "只能删除自己创建的自定义任务"
}
```

**权限限制：**
- 只能删除自己创建的自定义任务（`category = 'custom'`）
- 不能删除预设任务（`category = 'preset'`）
- 不能删除其他用户创建的任务

---

### 5. 标记任务完成/取消完成

**前端调用：**
```javascript
// 标记完成
export function completeTask(taskId, photoUrl = null, note = null) {
  return http.post('/api/challenge/complete', {
    taskId: taskId,
    completed: true,
    photoUrl: photoUrl,
    note: note
  });
}

// 取消完成
export function uncompleteTask(taskId) {
  return http.post('/api/challenge/complete', {
    taskId: taskId,
    completed: false
  });
}
```

**后端接口：**
- **地址：** `POST /api/challenge/complete`
- **请求头：** `Authorization: Bearer {token}`
- **请求体（标记完成）：**
```json
{
  "taskId": 1,
  "completed": true,
  "photoUrl": "https://example.com/photo.jpg",
  "note": "今天一起看了日出，很美！"
}
```

**请求体（取消完成）：**
```json
{
  "taskId": 1,
  "completed": false
}
```

**参数说明：**
- `taskId` (必填): 任务ID
- `completed` (必填): true=标记完成，false=取消完成
- `photoUrl` (可选): 完成时上传的照片URL
- `note` (可选): 完成时的备注说明

**成功响应（完成）：**
```json
{
  "success": true,
  "message": "任务已完成"
}
```

**成功响应（取消）：**
```json
{
  "success": true,
  "message": "已取消完成"
}
```

**说明：**
- 首次标记完成会创建新记录
- 重复操作会更新已有记录
- 取消完成会将状态改为 `pending`，并清空 `completedAt` 时间

---

### 6. 收藏/取消收藏任务

**前端调用：**
```javascript
// 收藏
export function favoriteTask(taskId) {
  return http.post('/api/challenge/favorite', {
    taskId: taskId,
    favorited: true
  });
}

// 取消收藏
export function unfavoriteTask(taskId) {
  return http.post('/api/challenge/favorite', {
    taskId: taskId,
    favorited: false
  });
}
```

**后端接口：**
- **地址：** `POST /api/challenge/favorite`
- **请求头：** `Authorization: Bearer {token}`
- **请求体：**
```json
{
  "taskId": 1,
  "favorited": true
}
```

**参数说明：**
- `taskId` (必填): 任务ID
- `favorited` (必填): true=收藏，false=取消收藏

**成功响应（收藏）：**
```json
{
  "success": true,
  "message": "已收藏"
}
```

**成功响应（取消收藏）：**
```json
{
  "success": true,
  "message": "已取消收藏"
}
```

---

## 🗄️ 数据库说明

### 预设任务列表（12个）

| ID | 任务名称 | 任务描述 |
|----|---------|---------|
| 1  | 一起看日出 | 找一个美好的清晨，一起迎接第一缕阳光 |
| 2  | 一起看日落 | 在夕阳西下时，享受彼此的陪伴 |
| 3  | 一起去教堂 | 在神圣的地方许下承诺 |
| 4  | 一起看星星 | 在晴朗的夜晚，一起数星星、许愿望 |
| 5  | 一起看电影 | 选一部你们都喜欢的电影，共度美好时光 |
| 6  | 一起牵手逛街 | 手牵手逛街，为对方挑选礼物 |
| 7  | 一起做饭 | 一起准备食材，一起烹饪美食 |
| 8  | 一起逛超市 | 像老夫老妻一样逛超市，挑选生活用品 |
| 9  | 一起逛家 | 一起逛家居店，布置温馨的家 |
| 10 | 一起看相声 | 欣赏传统曲艺，开怀大笑 |
| 11 | 一起打票 | 一起去看演出，提前买票期待 |
| 12 | 一起躺雨 | 在雨中漫步，感受浪漫 |

### 数据表结构

**1. challenge_tasks（任务表）**
- 存储预设任务和用户自定义任务
- 预设任务的 `category = 'preset'`，`task_index = 1-12`
- 自定义任务的 `category = 'custom'`，`created_by = 用户ID`

**2. user_challenge_records（用户记录表）**
- 存储用户的完成记录
- 包含完成状态、照片、备注、收藏等信息
- 每个用户对每个任务只能有一条记录（UNIQUE约束）

**3. user_challenge_progress（进度表）**
- 自动维护用户的整体进度
- 通过数据库触发器自动更新
- 无需手动操作

---

## ⚠️ 注意事项

### 1. 任务分类规则

- **预设任务**：系统自带12个，所有用户共享，不可删除
- **自定义任务**：用户自己创建，只有创建者可见和删除
- 用户看到的任务列表 = 12个预设 + 自己创建的自定义任务

### 2. 进度计算规则

- `totalTasks` = 12（预设） + 用户自定义任务数
- `completionRate` = (completedCount / totalTasks) × 100%
- 进度表由数据库触发器自动维护，无需前端手动更新

### 3. 照片上传流程

接口本身不处理照片上传，建议流程：
1. 前端先调用图片上传接口，获取图片URL
2. 将图片URL作为 `photoUrl` 参数传给完成任务接口
3. 后端只存储URL字符串

### 4. 收藏功能说明

- 收藏不影响完成状态
- 可以收藏未完成的任务（作为计划）
- 可以收藏已完成的任务（作为纪念）
- `favoritedCount` 在进度中自动统计

---

## 🔐 认证流程

### JWT Token 获取

```javascript
// 1. 用户登录
const loginResponse = await http.post('/api/login/wechat', {
  code: wxCode,
  nickName: userInfo.nickName,
  avatarUrl: userInfo.avatarUrl
});

// 2. 保存token
const token = loginResponse.data.token;
wx.setStorageSync('token', token);

// 3. 后续请求自动携带
headers: {
  'Authorization': `Bearer ${token}`
}
```

### Token 验证

后端会自动从 JWT token 中提取用户信息，无需前端传递 userId。

---

## 🎨 界面交互建议

### 任务列表展示

```javascript
// 按类别分组展示
const presetTasks = tasks.filter(t => t.category === 'preset');
const customTasks = tasks.filter(t => t.category === 'custom');

// 按完成状态分组
const completedTasks = tasks.filter(t => t.status === 'completed');
const pendingTasks = tasks.filter(t => t.status === 'pending');

// 只显示收藏的任务
const favoritedTasks = tasks.filter(t => t.isFavorited);
```

### 进度条展示

```html
<view class="progress-bar">
  <view class="progress-fill" :style="{width: progress.completionRate + '%'}"></view>
</view>
<text>已完成 {{progress.completedCount}}/{{progress.totalTasks}}</text>
<text>完成率 {{progress.completionRate}}%</text>
```

### 任务卡片设计建议

```html
<view class="task-card">
  <view class="task-header">
    <text class="task-name">{{task.taskName}}</text>
    <icon v-if="task.isFavorited" type="star" color="#FFD700"></icon>
  </view>
  <text class="task-desc">{{task.taskDescription}}</text>
  
  <!-- 未完成状态 -->
  <button v-if="task.status === 'pending'" @click="handleComplete">
    标记完成
  </button>
  
  <!-- 已完成状态 -->
  <view v-else class="completed-info">
    <image :src="task.photoUrl" mode="aspectFill"></image>
    <text>{{task.note}}</text>
    <text class="time">{{formatTime(task.completedAt)}}</text>
    <button @click="handleUncomplete">取消完成</button>
  </view>
  
  <!-- 操作按钮 -->
  <view class="actions">
    <button @click="toggleFavorite">
      {{task.isFavorited ? '取消收藏' : '收藏'}}
    </button>
    <button v-if="task.category === 'custom'" @click="handleDelete">
      删除
    </button>
  </view>
</view>
```

---

## 🧪 测试示例

### 使用 curl 测试

```bash
# 获取任务列表
curl -H "Authorization: Bearer your-token" \
  "http://localhost:8080/lovetime/api/challenge/tasks"

# 获取进度
curl -H "Authorization: Bearer your-token" \
  "http://localhost:8080/lovetime/api/challenge/progress"

# 添加自定义任务
curl -X POST \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json" \
  -d '{"taskName":"一起去旅行","taskDescription":"去一个从未去过的地方"}' \
  "http://localhost:8080/lovetime/api/challenge/task/add"

# 标记完成
curl -X POST \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json" \
  -d '{"taskId":1,"completed":true,"note":"今天一起看了日出！"}' \
  "http://localhost:8080/lovetime/api/challenge/complete"

# 收藏任务
curl -X POST \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json" \
  -d '{"taskId":1,"favorited":true}' \
  "http://localhost:8080/lovetime/api/challenge/favorite"

# 删除自定义任务
curl -X POST \
  -H "Authorization: Bearer your-token" \
  -H "Content-Type: application/json" \
  -d '{"taskId":13}' \
  "http://localhost:8080/lovetime/api/challenge/task/delete"
```

---

## 📦 配置文件示例

### config.js
```javascript
export default {
  API: {
    CHALLENGE: {
      LIST: '/api/challenge/tasks',
      PROGRESS: '/api/challenge/progress',
      ADD: '/api/challenge/task/add',
      DELETE: '/api/challenge/task/delete',
      COMPLETE: '/api/challenge/complete',
      FAVORITE: '/api/challenge/favorite'
    }
  }
}
```

### api.js
```javascript
import http from '@/utils/http.js';
import config from '@/config/config.js';

// 获取任务列表
export function getTasks() {
  return http.get(config.API.CHALLENGE.LIST);
}

// 获取进度
export function getProgress() {
  return http.get(config.API.CHALLENGE.PROGRESS);
}

// 添加任务
export function addTask(taskName, taskDescription = '') {
  return http.post(config.API.CHALLENGE.ADD, {
    taskName,
    taskDescription
  });
}

// 删除任务
export function deleteTask(taskId) {
  return http.post(config.API.CHALLENGE.DELETE, { taskId });
}

// 标记完成
export function completeTask(taskId, photoUrl = null, note = null) {
  return http.post(config.API.CHALLENGE.COMPLETE, {
    taskId,
    completed: true,
    photoUrl,
    note
  });
}

// 取消完成
export function uncompleteTask(taskId) {
  return http.post(config.API.CHALLENGE.COMPLETE, {
    taskId,
    completed: false
  });
}

// 收藏
export function favoriteTask(taskId, favorited) {
  return http.post(config.API.CHALLENGE.FAVORITE, {
    taskId,
    favorited
  });
}
```

---

## 🚀 部署清单

### 后端部署步骤

1. ✅ 执行数据库初始化脚本：`challenge_100_schema.sql`
2. ✅ 确认数据库中有12个预设任务
3. ✅ 确认 `ChallengeServlet.java` 已编译
4. ✅ Maven clean + package 构建项目
5. ✅ 重启 Tomcat 服务器
6. ✅ 验证接口可访问性

### 前端配置步骤

1. 配置 `config.js` 中的 API 路径
2. 实现 `api.js` 中的接口调用方法
3. 在页面中引入并调用 API
4. 处理成功和错误响应
5. 测试所有功能流程

---

## 🐛 常见问题

### Q1: 为什么进度表数据不更新？

**A:** 进度表由数据库触发器自动维护。如果数据不准确：
1. 检查触发器是否创建成功
2. 手动执行 SQL 重新计算进度
3. 确认 `user_challenge_records` 表数据正确

### Q2: 删除任务提示"只能删除自己创建的自定义任务"？

**A:** 检查：
1. 该任务是否为预设任务（`category = 'preset'`）
2. 该任务是否为其他用户创建（`created_by != 当前用户ID`）
3. Token 是否正确（是否为任务创建者的token）

### Q3: 任务列表中看不到其他用户创建的任务？

**A:** 这是设计如此。自定义任务只对创建者可见，确保每个用户的挑战列表是独立的。

### Q4: 如何实现情侣共同完成任务？

**A:** 当前版本每个用户独立完成任务。如需实现情侣共享：
1. 创建情侣关系表
2. 修改任务可见性规则
3. 添加情侣双方完成状态字段

---

## 📞 技术支持

### 日志查看

Tomcat 控制台会输出详细日志：
```
[ChallengeServlet] GET请求路径: /tasks
[ChallengeServlet] 获取任务列表，共 15 个任务
[ChallengeDAO] 用户 1 可见任务数: 15
```

### 错误排查

1. 检查 Tomcat 控制台错误信息
2. 检查浏览器 Network 面板
3. 验证数据库表数据
4. 确认 JWT token 有效性
5. 查看数据库触发器是否正常工作

---

## 📄 更新日志

**v1.0.0 (2025-10-31)**
- ✅ 初始版本发布
- ✅ 支持12个预设任务
- ✅ 支持自定义任务添加/删除
- ✅ 支持任务完成/取消完成
- ✅ 支持任务收藏功能
- ✅ 自动维护用户进度统计

---

**文档创建时间：** 2025-10-31  
**适用后端版本：** v1.0.0  
**维护者：** 后端开发团队
