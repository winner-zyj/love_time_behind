# 数据库连接配置说明

## 📋 已完成的工作

### 1. 添加MySQL依赖
在 `pom.xml` 中添加了MySQL驱动依赖。

### 2. 创建的文件

| 文件 | 说明 |
|------|------|
| `database.properties` | 数据库配置文件 |
| `DBUtil.java` | 数据库连接工具类 |
| `User.java` | 用户实体类 |
| `UserDAO.java` | 用户数据访问类 |
| `DBTestServlet.java` | 数据库测试接口 |

### 3. 更新的功能
- ✅ 微信登录接口现在会自动保存用户信息到数据库
- ✅ 新用户：插入数据库
- ✅ 老用户：更新昵称和头像

---

## 🔧 配置步骤

### 步骤1：修改数据库配置

打开文件：`src/main/resources/database.properties`

根据你的数据库信息修改：

```properties
# 数据库连接URL（修改数据库名、端口等）
db.url=jdbc:mysql://localhost:3306/lovetime?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true

# 数据库用户名（修改为你的用户名）
db.username=root

# 数据库密码（修改为你的密码）
db.password=your_password
```

### 步骤2：确认数据库和表存在

确保你的MySQL中：
1. ✅ 数据库 `lovetime` 已创建
2. ✅ 表 `users` 已创建

如果没有，可以执行以下SQL：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS lovetime DEFAULT CHARACTER SET utf8mb4;

-- 使用数据库
USE lovetime;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '微信openid',
    nickName VARCHAR(100) COMMENT '用户昵称',
    avatarUrl VARCHAR(500) COMMENT '用户头像URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 步骤3：重新部署项目

在IntelliJ IDEA中：
1. **Build** → **Rebuild Project**
2. 重启Tomcat服务器

---

## 🧪 测试数据库连接

### 方法1：使用测试接口

访问：
```
http://192.168.54.229:8080/lovetime/api/db/test
```

**成功响应示例**：
```json
{
  "connected": true,
  "success": true,
  "message": "数据库连接成功",
  "userCount": 3,
  "users": [
    {
      "id": 1,
      "code": "test_code_001",
      "nickName": "测试用户1",
      "avatarUrl": "https://...",
      "createdAt": "2025-10-28T17:00:00"
    }
  ]
}
```

**失败响应示例**：
```json
{
  "connected": false,
  "success": false,
  "message": "数据库连接失败"
}
```

### 方法2：测试微信登录

微信登录现在会自动将用户保存到数据库。

访问测试页面：
```
http://192.168.54.229:8080/lovetime/test-login.html
```

点击测试后，检查：
1. ✅ 控制台日志显示用户已保存
2. ✅ 数据库中查看 `users` 表，应该有新记录

---

## 📊 数据库操作说明

### UserDAO 提供的方法

| 方法 | 说明 |
|------|------|
| `findByCode(String code)` | 根据code查询用户 |
| `insert(User user)` | 插入新用户 |
| `update(User user)` | 更新用户信息 |
| `deleteById(Long id)` | 删除用户 |
| `findAll()` | 查询所有用户 |

### 使用示例

```java
// 创建DAO实例
UserDAO userDAO = new UserDAO();

// 查询用户
User user = userDAO.findByCode("odsBB1xHIGMxjj9-xQ695GWHDmus");

// 插入新用户
User newUser = new User("openid123", "张三", "https://...");
long id = userDAO.insert(newUser);

// 更新用户
user.setNickName("李四");
userDAO.update(user);

// 查询所有用户
List<User> users = userDAO.findAll();
```

---

## 🔍 查看日志

重启项目后，如果配置正确，会看到：

```
[数据库] 配置加载成功
[数据库] URL: jdbc:mysql://localhost:3306/lovetime...
[数据库] 用户名: root
[数据库] 连接成功
```

登录时会看到：

```
[微信登录] 开始处理用户数据...
[UserDAO] 用户插入成功，ID: 1
[微信登录] 新用户创建成功，ID: 1
```

---

## ⚠️ 常见问题

### Q1: 数据库连接失败
**A**: 检查：
- MySQL服务是否启动
- 数据库名、用户名、密码是否正确
- 端口号是否正确（默认3306）
- 防火墙是否允许连接

### Q2: 找不到配置文件
**A**: 确保 `database.properties` 在 `src/main/resources` 目录下，重新编译项目。

### Q3: 表不存在
**A**: 执行上面的SQL创建表，确保表名是 `users`。

### Q4: 字符编码问题
**A**: 确保：
- MySQL数据库字符集是 `utf8mb4`
- 配置URL中包含 `characterEncoding=utf8`

---

## 📝 总结

现在你的项目已经集成了数据库功能：

✅ 数据库连接工具类
✅ 用户实体和DAO
✅ 微信登录自动保存用户
✅ 数据库测试接口

**下一步**：
1. 修改 `database.properties` 配置
2. 重新部署项目
3. 访问 `/api/db/test` 测试连接
4. 测试微信登录，查看数据库是否有新记录
