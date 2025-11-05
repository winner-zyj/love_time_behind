# 情侣功能使用说明

## 概述
本文档说明如何使用情侣功能，包括邀请、绑定和共享功能的使用方法。

## 前提条件
1. 确保后端服务已启动
2. 确保数据库已初始化（执行了所有schema.sql文件）
3. 确保至少有两个用户账号

## 使用流程

### 1. 邀请流程

#### 步骤1：生成邀请码
邀请方（用户A）需要先生成邀请码：

```bash
curl -X POST "http://localhost:8080/lovetime/api/couple/invite/create" \
  -H "Authorization: Bearer {用户A的token}"
```

响应示例：
```json
{
  "success": true,
  "message": "邀请码生成成功",
  "inviteCode": "A1B2C3D4"
}
```

#### 步骤2：分享邀请码
将生成的邀请码通过任何方式分享给被邀请方（用户B）。

#### 步骤3：验证邀请码（可选）
被邀请方可以验证邀请码的有效性：

```bash
curl "http://localhost:8080/lovetime/api/couple/invite/validate?code=A1B2C3D4" \
  -H "Authorization: Bearer {用户B的token}"
```

响应示例：
```json
{
  "success": true,
  "message": "邀请码有效",
  "inviterId": 1,
  "inviterNickName": "用户A",
  "inviterAvatarUrl": "https://example.com/avatar-a.jpg"
}
```

#### 步骤4：接受邀请
被邀请方接受邀请，建立情侣关系：

```bash
curl -X POST "http://localhost:8080/lovetime/api/couple/bind/accept" \
  -H "Authorization: Bearer {用户B的token}" \
  -H "Content-Type: application/json" \
  -d '{"inviteCode": "A1B2C3D4"}'
```

响应示例：
```json
{
  "success": true,
  "message": "绑定成功",
  "relationshipId": 1
}
```

### 2. 验证绑定状态

任何一方都可以查询绑定状态：

```bash
curl "http://localhost:8080/lovetime/api/couple/status" \
  -H "Authorization: Bearer {用户token}"
```

绑定状态响应示例：
```json
{
  "success": true,
  "message": "已绑定",
  "isCouple": true,
  "partnerId": 2,
  "partnerNickName": "用户B",
  "partnerAvatarUrl": "https://example.com/avatar-b.jpg"
}
```

未绑定状态响应示例：
```json
{
  "success": true,
  "message": "未绑定",
  "isCouple": false
}
```

### 3. 共享功能使用

#### 3.1 甜蜜问答共享
当情侣双方回答相同问题时，系统会自动显示对方的答案：

```bash
# 用户A提交答案
curl -X POST "http://localhost:8080/lovetime/api/qna/submit" \
  -H "Authorization: Bearer {用户A的token}" \
  -H "Content-Type: application/json" \
  -d '{"questionId": 1, "answer": "咖啡厅"}'

# 用户B提交相同问题的答案
curl -X POST "http://localhost:8080/lovetime/api/qna/submit" \
  -H "Authorization: Bearer {用户B的token}" \
  -H "Content-Type: application/json" \
  -d '{"questionId": 1, "answer": "公园"}'

# 用户A再次提交或查询时，会看到用户B的答案
curl -X POST "http://localhost:8080/lovetime/api/qna/submit" \
  -H "Authorization: Bearer {用户A的token}" \
  -H "Content-Type: application/json" \
  -d '{"questionId": 1, "answer": "咖啡厅"}'
```

响应示例：
```json
{
  "success": true,
  "message": "答案提交成功",
  "answerId": 1,
  "partnerAnswer": "公园",
  "hasPartnerAnswer": true,
  "partnerAnsweredAt": "2025-11-04T10:30:00.000+00:00"
}
```

#### 3.2 一百事挑战共享
情侣双方可以查看对方的任务完成情况：

```bash
# 用户A完成一个任务
curl -X POST "http://localhost:8080/lovetime/api/challenge/complete" \
  -H "Authorization: Bearer {用户A的token}" \
  -H "Content-Type: application/json" \
  -d '{"taskId": 1, "completed": true, "note": "我们一起完成了这个任务！"}'

# 用户B查看任务列表时，可以看到用户A的完成状态
curl "http://localhost:8080/lovetime/api/challenge/tasks" \
  -H "Authorization: Bearer {用户B的token}"
```

#### 3.3 心形墙共享
情侣双方可以共同管理一个心形墙项目：

```bash
# 用户A创建一个心形墙项目
curl -X POST "http://localhost:8080/lovetime/api/heart-wall/projects" \
  -H "Authorization: Bearer {用户A的token}" \
  -H "Content-Type: application/json" \
  -d '{"projectName": "我们的回忆", "description": "记录我们的美好时光"}'

# 用户B可以查看并上传照片到用户A创建的项目
curl -X POST "http://localhost:8080/lovetime/api/heart-wall/photos" \
  -H "Authorization: Bearer {用户B的token}" \
  -H "Content-Type: application/json" \
  -d '{"projectId": 1, "photoUrl": "https://example.com/photo1.jpg", "positionIndex": 1, "caption": "我们的第一次约会"}'

# 用户A和用户B都可以查看项目中的所有照片
curl "http://localhost:8080/lovetime/api/heart-wall/projects/1?action=photos" \
  -H "Authorization: Bearer {用户A的token}"
```

### 4. 解绑关系

当情侣关系需要解除时，任何一方都可以发起解绑：

```bash
curl -X POST "http://localhost:8080/lovetime/api/couple/unbind" \
  -H "Authorization: Bearer {用户token}"
```

响应示例：
```json
{
  "success": true,
  "message": "解绑成功"
}
```

解绑后，双方将无法再查看对方的共享信息。

## 测试建议

### 1. 正常流程测试
- 完整执行邀请、绑定、共享、解绑流程
- 验证各步骤的响应数据正确性
- 检查数据库中数据的一致性

### 2. 异常情况测试
- 使用无效的邀请码
- 尝试邀请已绑定的用户
- 尝试解绑不存在的关系
- 未登录状态下访问接口

### 3. 权限测试
- 验证非情侣用户无法访问共享内容
- 验证用户只能删除自己创建的内容
- 验证情侣用户可以共同管理共享内容

### 4. 并发测试
- 多用户同时操作同一资源
- 高并发情况下的数据一致性

## 常见问题

### 1. 邀请码无效
**问题**: 验证邀请码时返回"邀请码无效"
**解决方案**: 
- 确认邀请码正确无误
- 确认邀请方已生成邀请码
- 确认邀请方未被其他用户绑定

### 2. 无法建立情侣关系
**问题**: 接受邀请时返回"绑定失败"
**解决方案**:
- 确认双方都未与其他用户绑定
- 检查数据库连接是否正常
- 查看服务端日志获取详细错误信息

### 3. 共享功能不工作
**问题**: 无法看到情侣的答案或内容
**解决方案**:
- 确认双方已正确绑定
- 检查数据库中的couple_relationships表数据
- 确认相关数据库函数正常工作

### 4. 权限问题
**问题**: 无法访问某些功能或资源
**解决方案**:
- 确认请求头中包含有效的Authorization token
- 确认用户具有相应的操作权限
- 检查服务端权限验证逻辑

## 技术支持

如遇问题，请检查：
1. 服务端控制台日志
2. 数据库连接状态
3. JWT token的有效性
4. 数据库中相关表的数据完整性

**日志位置**: Tomcat控制台会输出详细的请求日志，格式如：
```
[CoupleServlet] 用户 1 生成邀请码: A1B2C3D4
[CoupleServlet] 用户 2 接受邀请，与用户 1 绑定成功
[QnaServlet] 用户 1 提交答案，找到情侣答案
```