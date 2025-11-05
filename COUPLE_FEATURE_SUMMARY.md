# 情侣功能实现总结

## 已实现的功能

### 1. 情侣关系管理API (新增)
实现了完整的情侣关系管理功能，包括：
- 生成邀请码
- 验证邀请码
- 接受邀请（绑定）
- 查询绑定状态
- 解绑关系

### 2. 甜蜜问答功能（已存在，增强）
实现了情侣共享的甜蜜问答功能：
- 用户可以回答预设问题
- 系统自动获取情侣的答案进行对比
- 支持自定义问题
- 查看历史答题记录

### 3. 一百事挑战功能（已存在，增强）
实现了情侣共享的挑战任务功能：
- 预设12个挑战任务
- 支持用户自定义任务
- 任务完成状态独立记录
- 进度统计和展示

### 4. 心形墙功能（已存在，增强）
实现了情侣共享的心形墙功能：
- 创建心形墙项目
- 上传照片到指定位置
- 情侣双方可以共同管理一个心形墙项目
- 照片展示和管理

## API接口说明

### 情侣关系管理接口
所有接口都需要在请求头携带 `Authorization: Bearer {token}`。

#### 1. 生成邀请码
- **接口**: POST /api/couple/invite/create
- **说明**: 为当前用户生成一个邀请码，用于邀请他人成为情侣

#### 2. 验证邀请码
- **接口**: GET /api/couple/invite/validate?code=XXX
- **说明**: 验证邀请码的有效性，返回邀请方信息

#### 3. 接受邀请（绑定）
- **接口**: POST /api/couple/bind/accept
- **请求体**: { "inviteCode": "XXX" }
- **说明**: 接受邀请，建立情侣关系

#### 4. 查询绑定状态
- **接口**: GET /api/couple/status
- **说明**: 查询当前用户的情侣绑定状态

#### 5. 解绑关系
- **接口**: POST /api/couple/unbind
- **说明**: 解除情侣关系

### 甜蜜问答接口
#### 1. 获取问题列表
- **接口**: GET /api/qna/questions

#### 2. 获取当前问题
- **接口**: GET /api/qna/current

#### 3. 提交答案
- **接口**: POST /api/qna/submit
- **说明**: 提交答案后会自动返回情侣的答案（如果已回答）

#### 4. 获取历史答案
- **接口**: GET /api/qna/history

#### 5. 添加自定义问题
- **接口**: POST /api/qna/question/add

#### 6. 删除自定义问题
- **接口**: POST /api/qna/question/delete

### 一百事挑战接口
#### 1. 获取任务列表
- **接口**: GET /api/challenge/tasks

#### 2. 获取用户进度
- **接口**: GET /api/challenge/progress

#### 3. 添加自定义任务
- **接口**: POST /api/challenge/task/add

#### 4. 删除自定义任务
- **接口**: POST /api/challenge/task/delete

#### 5. 标记任务完成/取消完成
- **接口**: POST /api/challenge/complete

#### 6. 收藏/取消收藏任务
- **接口**: POST /api/challenge/favorite

### 心形墙接口
#### 1. 创建项目
- **接口**: POST /api/heart-wall/projects

#### 2. 上传照片
- **接口**: POST /api/heart-wall/photos

#### 3. 获取用户项目列表
- **接口**: GET /api/heart-wall/projects

#### 4. 获取项目详情
- **接口**: GET /api/heart-wall/projects/{projectId}

#### 5. 获取项目照片列表
- **接口**: GET /api/heart-wall/projects/{projectId}?action=photos

#### 6. 获取下一个可用位置
- **接口**: GET /api/heart-wall/next-position?projectId=XXX

#### 7. 更新项目
- **接口**: PUT /api/heart-wall/projects/{projectId}

#### 8. 更新照片
- **接口**: PUT /api/heart-wall/photos/{photoId}

#### 9. 删除项目
- **接口**: DELETE /api/heart-wall/projects/{projectId}

#### 10. 删除照片
- **接口**: DELETE /api/heart-wall/photos/{photoId}

#### 11. 清空项目照片
- **接口**: PUT /api/heart-wall/clear-photos?projectId=XXX

## 核心实现细节

### 1. 情侣关系数据库设计
- 使用 `couple_relationships` 表存储情侣关系
- 通过数据库触发器确保一对一关系
- 提供数据库函数 `get_partner_id()` 和 `is_couple()` 简化查询

### 2. 共享机制实现
- **甜蜜问答**: 通过 `get_partner_answer()` 数据库函数获取情侣答案
- **一百事挑战**: 每个用户的任务进度独立记录，但可以查看情侣的完成情况
- **心形墙**: 通过 `hasPermissionForProject()` 方法检查用户是否有权限访问项目

### 3. 权限控制
- 所有接口都通过JWT Token验证用户身份
- 对于共享功能，会检查用户是否为情侣关系
- 不同操作有不同的权限要求（创建者、情侣等）

## 使用流程

### 邀请流程
1. 用户A点击"邀请另一半"
2. 进入邀请页面，点击"生成邀请码"
3. 点击"分享给TA"或使用右上角菜单转发小程序卡片
4. 用户B收到分享，点击卡片打开小程序
5. 自动进入邀请页面，显示用户A的信息
6. 用户B点击"接受邀请"即可完成绑定

### 绑定后功能
- 双方在"我们"页面都能看到对方信息
- 显示在一起的天数
- 甜蜜问答、一百事挑战、心形墙等功能实现数据共享
- 可以随时解绑关系

## 技术实现要点

### 1. 新增类文件
- `CoupleRelationship.java` - 情侣关系实体类
- `CoupleRelationshipDAO.java` - 情侣关系数据访问类
- `CoupleRequest.java` - 情侣关系请求DTO
- `CoupleResponse.java` - 情侣关系响应DTO
- `CoupleServlet.java` - 情侣关系接口控制器

### 2. 修改的类文件
- `HeartWallServlet.java` - 增加情侣权限检查
- `QnaServlet.java` - 优化情侣答案获取
- `web.xml` - 添加情侣关系接口映射

### 3. 数据库增强
- 已有的数据库模式支持情侣共享功能
- 通过数据库函数简化情侣关系查询
- 触发器确保数据一致性

## 测试建议

1. 验证邀请码生成和验证功能
2. 测试情侣绑定和解绑流程
3. 验证共享功能在各种场景下的正确性
4. 测试权限控制的准确性
5. 验证异常情况的处理

## 注意事项

1. 所有接口都需要有效的JWT Token
2. 情侣关系是一对一的，不能同时与多人建立关系
3. 心形墙项目只能由创建者删除
4. 照片只能由上传者删除
5. 自定义问题和任务只能由创建者删除