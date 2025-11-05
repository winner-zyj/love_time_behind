# API接口映射文档

## 概述
本文档列出了所有后端API接口的详细信息，包括路径、方法、请求参数和响应格式。

## 1. 微信登录接口

### 1.1 微信登录
- **路径**: POST /api/login/wechat
- **说明**: 微信小程序登录接口
- **请求头**: 无特殊要求
- **请求体**:
  ```json
  {
    "code": "微信登录凭证",
    "userInfo": {
      "nickName": "用户昵称",
      "avatarUrl": "用户头像URL"
    }
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "登录成功",
    "data": {
      "token": "JWT令牌",
      "openid": "用户唯一标识",
      "session_key": "会话密钥"
    }
  }
  ```

## 2. 情侣关系管理接口

### 2.1 生成邀请码
- **路径**: POST /api/couple/invite/create
- **说明**: 为当前用户生成一个邀请码，用于邀请他人成为情侣
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "邀请码生成成功",
    "inviteCode": "邀请码"
  }
  ```

### 2.2 验证邀请码
- **路径**: GET /api/couple/invite/validate?code={inviteCode}
- **说明**: 验证邀请码的有效性，返回邀请方信息
- **请求头**: Authorization: Bearer {token}
- **请求参数**: code - 邀请码
- **响应**:
  ```json
  {
    "success": true,
    "message": "邀请码有效",
    "inviterId": "邀请方用户ID",
    "inviterNickName": "邀请方昵称",
    "inviterAvatarUrl": "邀请方头像URL"
  }
  ```

### 2.3 接受邀请（绑定）
- **路径**: POST /api/couple/bind/accept
- **说明**: 接受邀请，建立情侣关系
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "inviteCode": "邀请码"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "绑定成功",
    "relationshipId": "关系ID"
  }
  ```

### 2.4 查询绑定状态
- **路径**: GET /api/couple/status
- **说明**: 查询当前用户的情侣绑定状态
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "已绑定",
    "isCouple": true,
    "partnerId": "伴侣ID",
    "partnerNickName": "伴侣昵称",
    "partnerAvatarUrl": "伴侣头像URL"
  }
  ```

### 2.5 解绑关系
- **路径**: POST /api/couple/unbind
- **说明**: 解除情侣关系
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "解绑成功"
  }
  ```

## 3. 甜蜜问答接口

### 3.1 获取问题列表
- **路径**: GET /api/qna/questions
- **说明**: 获取所有问题列表（预设问题+自定义问题）
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取成功",
    "questions": [
      {
        "id": 1,
        "questionText": "问题内容",
        "category": "preset|custom",
        "createdBy": "创建者ID（自定义问题时）",
        "createdAt": "创建时间"
      }
    ]
  }
  ```

### 3.2 获取当前问题
- **路径**: GET /api/qna/current
- **说明**: 获取用户当前应该回答的问题
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取成功",
    "question": {
      "id": 1,
      "questionText": "问题内容"
    },
    "answeredCount": "已回答问题数",
    "totalCount": "总问题数"
  }
  ```

### 3.3 提交答案
- **路径**: POST /api/qna/submit
- **说明**: 提交答案，同时返回情侣的答案（如果已回答）
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "questionId": "问题ID",
    "answer": "答案内容"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "答案提交成功",
    "answerId": "答案ID",
    "partnerAnswer": "伴侣答案（如果已回答）",
    "hasPartnerAnswer": true|false,
    "partnerAnsweredAt": "伴侣回答时间（如果有）"
  }
  ```

### 3.4 获取历史答案
- **路径**: GET /api/qna/history
- **说明**: 获取用户的历史答案记录
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取成功",
    "history": [
      {
        "id": "答案ID",
        "questionId": "问题ID",
        "answer": "答案内容",
        "answeredAt": "回答时间",
        "updatedAt": "更新时间",
        "questionText": "问题内容",
        "questionCategory": "问题类型"
      }
    ],
    "totalCount": "总记录数"
  }
  ```

### 3.5 添加自定义问题
- **路径**: POST /api/qna/question/add
- **说明**: 添加用户自定义问题
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "text": "问题内容"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "问题添加成功",
    "question": {
      "id": "问题ID",
      "questionText": "问题内容",
      "category": "custom",
      "createdBy": "创建者ID",
      "createdAt": "创建时间"
    }
  }
  ```

### 3.6 删除自定义问题
- **路径**: POST /api/qna/question/delete
- **说明**: 删除用户自定义问题
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "questionId": "问题ID"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "问题删除成功"
  }
  ```

## 4. 一百事挑战接口

### 4.1 获取任务列表
- **路径**: GET /api/challenge/tasks
- **说明**: 获取用户可见的所有任务（预设任务+自定义任务）
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取成功",
    "tasks": [
      {
        "id": "任务ID",
        "taskName": "任务名称",
        "taskDescription": "任务描述",
        "category": "preset|custom",
        "taskIndex": "任务序号（预设任务）",
        "createdBy": "创建者ID（自定义任务）",
        "isActive": true|false,
        "createdAt": "创建时间",
        "updatedAt": "更新时间",
        "userRecord": {
          "status": "pending|completed",
          "photoUrl": "照片URL",
          "note": "备注",
          "isFavorited": true|false,
          "completedAt": "完成时间"
        }
      }
    ]
  }
  ```

### 4.2 获取用户进度
- **路径**: GET /api/challenge/progress
- **说明**: 获取用户的挑战进度
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取成功",
    "progress": {
      "id": "进度ID",
      "userId": "用户ID",
      "totalTasks": "总任务数",
      "completedCount": "已完成数量",
      "favoritedCount": "收藏数量",
      "completionRate": "完成率",
      "lastActiveAt": "最后活跃时间",
      "createdAt": "创建时间",
      "updatedAt": "更新时间"
    }
  }
  ```

### 4.3 添加自定义任务
- **路径**: POST /api/challenge/task/add
- **说明**: 添加用户自定义任务
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "taskName": "任务名称",
    "taskDescription": "任务描述（可选）"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "任务添加成功",
    "task": {
      "id": "任务ID",
      "taskName": "任务名称",
      "taskDescription": "任务描述",
      "category": "custom",
      "createdBy": "创建者ID",
      "isActive": true,
      "createdAt": "创建时间",
      "updatedAt": "更新时间"
    }
  }
  ```

### 4.4 删除自定义任务
- **路径**: POST /api/challenge/task/delete
- **说明**: 删除用户自定义任务
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "taskId": "任务ID"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "任务删除成功"
  }
  ```

### 4.5 标记任务完成/取消完成
- **路径**: POST /api/challenge/complete
- **说明**: 标记任务为完成或取消完成状态
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "taskId": "任务ID",
    "completed": true|false,
    "photoUrl": "照片URL（可选）",
    "note": "备注（可选）"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "任务已完成|已取消完成"
  }
  ```

### 4.6 收藏/取消收藏任务
- **路径**: POST /api/challenge/favorite
- **说明**: 收藏或取消收藏任务
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "taskId": "任务ID",
    "favorited": true|false
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "已收藏|已取消收藏"
  }
  ```

## 5. 心形墙接口

### 5.1 创建项目
- **路径**: POST /api/heart-wall/projects
- **说明**: 创建一个新的心形墙项目
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "projectName": "项目名称",
    "description": "项目描述（可选）",
    "isPublic": true|false（可选，默认false）,
    "maxPhotos": "最大照片数量（可选，默认40）"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "心形墙项目创建成功",
    "project": {
      "id": "项目ID",
      "userId": "创建者ID",
      "projectName": "项目名称",
      "description": "项目描述",
      "photoCount": 0,
      "maxPhotos": 40,
      "isPublic": false,
      "createdAt": "创建时间",
      "updatedAt": "更新时间"
    }
  }
  ```

### 5.2 上传照片
- **路径**: POST /api/heart-wall/photos
- **说明**: 上传照片到心形墙项目
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "projectId": "项目ID",
    "photoUrl": "照片URL",
    "thumbnailUrl": "缩略图URL（可选）",
    "positionIndex": "位置索引（可选，1-40）",
    "caption": "照片说明（可选）",
    "takenDate": "拍摄日期（可选，格式：YYYY-MM-DD）"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "照片上传成功",
    "photo": {
      "id": "照片ID",
      "projectId": "项目ID",
      "userId": "上传者ID",
      "photoUrl": "照片URL",
      "thumbnailUrl": "缩略图URL",
      "positionIndex": "位置索引",
      "caption": "照片说明",
      "takenDate": "拍摄日期",
      "uploadedAt": "上传时间",
      "updatedAt": "更新时间"
    }
  }
  ```

### 5.3 获取用户项目列表
- **路径**: GET /api/heart-wall/projects
- **说明**: 获取用户创建的所有心形墙项目
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取用户心形墙项目列表成功",
    "projects": [
      {
        "id": "项目ID",
        "userId": "创建者ID",
        "projectName": "项目名称",
        "description": "项目描述",
        "photoCount": "已上传照片数量",
        "maxPhotos": "最大照片数量",
        "coverPhotoUrl": "封面照片URL",
        "isPublic": false,
        "createdAt": "创建时间",
        "updatedAt": "更新时间",
        "userNickName": "创建者昵称",
        "userAvatarUrl": "创建者头像URL"
      }
    ]
  }
  ```

### 5.4 获取项目详情
- **路径**: GET /api/heart-wall/projects/{projectId}
- **说明**: 获取心形墙项目的详细信息
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取心形墙项目详情成功",
    "project": {
      "id": "项目ID",
      "userId": "创建者ID",
      "projectName": "项目名称",
      "description": "项目描述",
      "photoCount": "已上传照片数量",
      "maxPhotos": "最大照片数量",
      "coverPhotoUrl": "封面照片URL",
      "isPublic": false,
      "createdAt": "创建时间",
      "updatedAt": "更新时间",
      "userNickName": "创建者昵称",
      "userAvatarUrl": "创建者头像URL"
    }
  }
  ```

### 5.5 获取项目照片列表
- **路径**: GET /api/heart-wall/projects/{projectId}?action=photos[&page=1&pageSize=20]
- **说明**: 获取心形墙项目中的所有照片
- **请求头**: Authorization: Bearer {token}
- **请求参数**: 
  - action=photos（必需）
  - page=页码（可选，默认1）
  - pageSize=每页数量（可选，默认20）
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取心形墙照片列表成功",
    "photos": [
      {
        "id": "照片ID",
        "projectId": "项目ID",
        "userId": "上传者ID",
        "photoUrl": "照片URL",
        "thumbnailUrl": "缩略图URL",
        "positionIndex": "位置索引",
        "caption": "照片说明",
        "takenDate": "拍摄日期",
        "uploadedAt": "上传时间",
        "updatedAt": "更新时间",
        "userNickName": "上传者昵称",
        "userAvatarUrl": "上传者头像URL"
      }
    ],
    "photoCount": "总照片数"
  }
  ```

### 5.6 获取下一个可用位置
- **路径**: GET /api/heart-wall/next-position?projectId={projectId}
- **说明**: 获取心形墙项目中下一个可用的照片位置
- **请求头**: Authorization: Bearer {token}
- **请求参数**: projectId - 项目ID
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取下一个可用位置成功",
    "nextPosition": "下一个可用位置索引（1-40）"
  }
  ```

### 5.7 更新项目
- **路径**: PUT /api/heart-wall/projects/{projectId}
- **说明**: 更新心形墙项目信息
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "projectName": "项目名称（可选）",
    "description": "项目描述（可选）",
    "isPublic": true|false（可选）,
    "maxPhotos": "最大照片数量（可选，设置为0可清空所有照片）"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "心形墙项目更新成功",
    "project": {
      "id": "项目ID",
      "userId": "创建者ID",
      "projectName": "项目名称",
      "description": "项目描述",
      "photoCount": "已上传照片数量",
      "maxPhotos": "最大照片数量",
      "coverPhotoUrl": "封面照片URL",
      "isPublic": false,
      "createdAt": "创建时间",
      "updatedAt": "更新时间"
    }
  }
  ```

### 5.8 更新照片
- **路径**: PUT /api/heart-wall/photos/{photoId}
- **说明**: 更新心形墙照片信息
- **请求头**: Authorization: Bearer {token}
- **请求体**:
  ```json
  {
    "photoUrl": "照片URL（可选）",
    "thumbnailUrl": "缩略图URL（可选）",
    "positionIndex": "位置索引（可选，1-40）",
    "caption": "照片说明（可选）",
    "takenDate": "拍摄日期（可选，格式：YYYY-MM-DD）"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "照片更新成功",
    "photo": {
      "id": "照片ID",
      "projectId": "项目ID",
      "userId": "上传者ID",
      "photoUrl": "照片URL",
      "thumbnailUrl": "缩略图URL",
      "positionIndex": "位置索引",
      "caption": "照片说明",
      "takenDate": "拍摄日期",
      "uploadedAt": "上传时间",
      "updatedAt": "更新时间"
    }
  }
  ```

### 5.9 删除项目
- **路径**: DELETE /api/heart-wall/projects/{projectId}
- **说明**: 删除心形墙项目（仅创建者可以删除）
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "心形墙项目删除成功"
  }
  ```

### 5.10 删除照片
- **路径**: DELETE /api/heart-wall/photos/{photoId}
- **说明**: 删除心形墙照片（仅上传者可以删除）
- **请求头**: Authorization: Bearer {token}
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "照片删除成功"
  }
  ```

### 5.11 清空项目照片
- **路径**: PUT /api/heart-wall/clear-photos?projectId={projectId}
- **说明**: 清空心形墙项目中的所有照片（仅创建者可以操作）
- **请求头**: Authorization: Bearer {token}
- **请求参数**: projectId - 项目ID
- **请求体**: 无
- **响应**:
  ```json
  {
    "success": true,
    "message": "项目照片已清空"
  }
  ```

## 错误响应格式

所有接口在出错时都会返回以下格式的响应：
```json
{
  "success": false,
  "message": "错误信息描述"
}
```

常见HTTP状态码：
- 200 - 成功
- 400 - 请求参数错误
- 401 - 未认证
- 403 - 无权限
- 404 - 资源不存在
- 500 - 服务器错误