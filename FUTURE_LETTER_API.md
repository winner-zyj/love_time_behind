# 未来情书API接口文档

## 接口前缀
所有接口都以 `/api/future-letter` 为前缀。

## 1. 创建未来情书
**POST /**

### 请求参数
```json
{
  "receiverId": 2,              // 接收者用户ID（可选，当deliveryMethod为PARTNER时使用）
  "title": "给未来的你",         // 情书标题（必填）
  "content": "亲爱的，希望你...", // 情书内容（必填）
  "deliveryMethod": "PARTNER",  // 发送方式：目前只支持PARTNER（情侣）
  "scheduledDate": "2025-07-22",  // 预计发送日期（必填，格式：YYYY-MM-DD）
  "scheduledTime": "00:00:00",    // 预计发送时间（可选，格式：HH:MM:SS，默认为00:00:00）
  "status": "DRAFT",             // 状态：DRAFT（草稿）、SCHEDULED（已安排）
  "backgroundImage": "https://example.com/image.jpg" // 背景图片URL（可选）
}
```

### 响应示例
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "title": "给未来的你",
    "content": "亲爱的，希望你...",
    "deliveryMethod": "PARTNER",
    "scheduledDate": "2025-07-22",
    "scheduledTime": "00:00:00",
    "status": "DRAFT",
    "createdAt": "2023-07-22T10:00:00.000+00:00",
    "updatedAt": "2023-07-22T10:00:00.000+00:00"
  }
}
```

## 2. 获取未来情书列表
**GET /**

### 请求参数
- status: 筛选状态（可选，DRAFT、SCHEDULED、SENT等）

### 响应示例
```json
{
  "success": true,
  "message": "获取未来情书列表成功",
  "data": [
    {
      "id": 1,
      "senderId": 1,
      "receiverId": 2,
      "title": "给未来的你",
      "content": "亲爱的，希望你...",
      "deliveryMethod": "PARTNER",
      "scheduledDate": "2025-07-22",
      "scheduledTime": "00:00:00",
      "status": "SCHEDULED",
      "createdAt": "2023-07-22T10:00:00.000+00:00",
      "updatedAt": "2023-07-22T10:00:00.000+00:00"
    }
  ]
}
```

## 3. 获取已发送情书列表
**GET /sent**

### 响应示例
```json
{
  "success": true,
  "message": "获取已发送情书列表成功",
  "data": [
    {
      "id": 1,
      "senderId": 1,
      "receiverId": 2,
      "title": "给未来的你",
      "content": "亲爱的，希望你...",
      "deliveryMethod": "PARTNER",
      "scheduledDate": "2025-07-22",
      "scheduledTime": "00:00:00",
      "status": "SENT",
      "sentAt": "2025-07-22T00:00:00.000+00:00",
      "createdAt": "2023-07-22T10:00:00.000+00:00",
      "updatedAt": "2025-07-22T00:00:00.000+00:00"
    }
  ]
}
```

## 4. 获取收到情书列表
**GET /received**

### 响应示例
```json
{
  "success": true,
  "message": "获取收到情书列表成功",
  "data": [
    {
      "id": 1,
      "senderId": 1,
      "receiverId": 2,
      "title": "给未来的你",
      "content": "亲爱的，希望你...",
      "deliveryMethod": "PARTNER",
      "scheduledDate": "2025-07-22",
      "scheduledTime": "00:00:00",
      "status": "SENT",
      "sentAt": "2025-07-22T00:00:00.000+00:00",
      "createdAt": "2023-07-22T10:00:00.000+00:00",
      "updatedAt": "2025-07-22T00:00:00.000+00:00"
    }
  ]
}
```

## 5. 获取统计信息
**GET /stats**

### 响应示例
```json
{
  "success": true,
  "message": "获取统计信息成功",
  "data": {
    "draftCount": 2,
    "scheduledCount": 3,
    "sentCount": 1
  }
}
```

## 6. 获取情书详情
**GET /{id}**

### 响应示例
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "title": "给未来的你",
    "content": "亲爱的，希望你...",
    "deliveryMethod": "PARTNER",
    "scheduledDate": "2025-07-22",
    "scheduledTime": "00:00:00",
    "status": "SCHEDULED",
    "createdAt": "2023-07-22T10:00:00.000+00:00",
    "updatedAt": "2023-07-22T10:00:00.000+00:00"
  }
}
```

## 7. 更新未来情书
**PUT /{id}**

### 请求参数
同创建接口

### 响应示例
同创建接口

## 8. 发送未来情书（立即发送）
**POST /{id}/send**

### 响应示例
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "title": "给未来的你",
    "content": "亲爱的，希望你...",
    "deliveryMethod": "PARTNER",
    "scheduledDate": "2025-07-22",
    "scheduledTime": "00:00:00",
    "status": "SENT",
    "sentAt": "2023-07-22T10:00:00.000+00:00",
    "createdAt": "2023-07-22T10:00:00.000+00:00",
    "updatedAt": "2023-07-22T10:00:00.000+00:00"
  }
}
```

## 9. 删除未来情书
**DELETE /{id}**

### 响应示例
```json
{
  "success": true,
  "message": "删除未来情书成功"
}
```