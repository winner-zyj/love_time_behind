# 微信授权登录功能说明

## 功能概述
实现了完整的微信小程序授权登录功能，包括：
- 接收前端发送的微信登录code
- 调用微信服务器获取openid和session_key
- 生成JWT token
- 返回登录结果给前端

## 项目结构

```
src/main/java/com/abc/love_time/
├── servlet/
│   └── WeChatLoginServlet.java          # 微信登录接口
├── dto/
│   ├── WeChatLoginRequest.java          # 登录请求DTO
│   ├── WeChatLoginResponse.java         # 登录响应DTO
│   └── WeChatSession.java               # 微信session数据DTO
└── util/
    ├── JwtUtil.java                     # JWT工具类
    └── WeChatApiClient.java             # 微信API客户端

src/main/resources/
└── wechat.properties                    # 微信配置文件
```

## API接口说明

### 1. 登录接口
**URL**: `POST /api/login/wechat`

**请求示例**:
```json
{
  "code": "081xYz0w3wkTiw2TID1w3BW8Jd0xYz0f",
  "nickName": "微信用户",
  "avatarUrl": "https://thirdwx.qlogo.cn/..."
}
```

**成功响应**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "openid": "oABC123xyz...",
    "session_key": "HyVFkGl5F5..."
  }
}
```

**失败响应**:
```json
{
  "success": false,
  "message": "登录失败: 错误信息",
  "data": null
}
```

## 配置说明

### 1. 修改微信配置
打开 `src/main/java/com/abc/love_time/util/WeChatApiClient.java`，修改以下常量：

```java
private static final String APP_ID = "your_app_id";      // 替换为你的AppID
private static final String APP_SECRET = "your_app_secret"; // 替换为你的AppSecret
```

或者修改 `src/main/resources/wechat.properties` 配置文件。

### 2. 获取微信AppID和AppSecret
1. 登录微信公众平台：https://mp.weixin.qq.com
2. 进入"开发" -> "开发管理" -> "开发设置"
3. 复制AppID和AppSecret

## 工作流程

1. **前端发送请求**
   - 前端通过wx.login()获取code
   - 连同用户信息一起发送到后端

2. **后端处理流程**
   ```
   接收请求 
   → 解析JSON数据 
   → 验证code参数 
   → 调用微信API获取openid和session_key 
   → 生成JWT token 
   → 返回响应
   ```

3. **微信API调用**
   - URL: `https://api.weixin.qq.com/sns/jscode2session`
   - 参数: appid, secret, js_code, grant_type
   - 返回: openid, session_key, unionid

4. **JWT生成**
   - 使用HS256算法
   - 包含openid和session_key
   - 有效期7天

## 依赖说明

已在pom.xml中添加以下依赖：

```xml
<!-- JSON处理 -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<!-- JWT支持 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-gson</artifactId>
    <version>0.11.5</version>
</dependency>
```

## 测试接口

### 使用curl测试
```bash
curl -X POST http://localhost:8080/api/login/wechat \
  -H "Content-Type: application/json" \
  -d '{
    "code": "081xYz0w3wkTiw2TID1w3BW8Jd0xYz0f",
    "nickName": "微信用户",
    "avatarUrl": "https://thirdwx.qlogo.cn/..."
  }'
```

### 使用Postman测试
1. 方法: POST
2. URL: http://localhost:8080/api/login/wechat
3. Headers: Content-Type: application/json
4. Body: 选择raw，格式JSON，填入上述请求数据

## 安全建议

1. **生产环境配置**
   - 不要将AppID和AppSecret硬编码在代码中
   - 使用配置文件或环境变量管理敏感信息
   - JWT密钥使用强随机字符串

2. **HTTPS部署**
   - 生产环境必须使用HTTPS
   - 防止token在传输过程中被截获

3. **Token验证**
   - 在需要鉴权的接口中验证JWT token
   - 可以创建一个Filter统一处理token验证

4. **session_key保护**
   - session_key非常敏感，不要明文存储
   - 实际生产中建议只返回token，不返回session_key

## 后续优化建议

1. **用户信息存储**
   - 将openid、nickName、avatarUrl存入数据库
   - 建立用户表管理用户信息

2. **Token刷新机制**
   - 实现token刷新接口
   - 支持token过期后自动刷新

3. **日志记录**
   - 记录登录日志
   - 记录异常信息便于排查问题

4. **配置外部化**
   - 使用Spring Boot等框架管理配置
   - 支持多环境配置

## 常见问题

### Q: code已被使用
A: 微信的code只能使用一次，每次登录都需要重新获取

### Q: 调用微信API超时
A: 检查网络连接，确保服务器可以访问微信服务器

### Q: 40163错误
A: code无效，可能已过期或已使用

### Q: 40013错误  
A: AppID或AppSecret配置错误
