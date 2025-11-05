# 心形墙API接口文档

## 基础URL
所有API接口都以 `/api/heart-wall` 为前缀。

## 认证方式
所有接口都需要在请求头中包含有效的JWT Token：
```
Authorization: Bearer <token>
```

## 项目相关接口

### 1. 创建心形墙项目
- **URL**: `POST /api/heart-wall/projects`
- **请求参数**:
```json
{
  "projectName": "我们的回忆",
  "description": "记录我们的美好时光",
  "isPublic": false,
  "maxPhotos": 40
}
```
- **响应示例**:
```json
{
  "success": true,
  "message": "心形墙项目创建成功",
  "project": {
    "id": 1,
    "userId": 1,
    "projectName": "我们的回忆",
    "description": "记录我们的美好时光",
    "photoCount": 0,
    "maxPhotos": 40,
    "coverPhotoUrl": null,
    "isPublic": false,
    "createdAt": "2025-11-03T10:00:00.000+00:00",
    "updatedAt": "2025-11-03T10:00:00.000+00:00"
  }
}
```

### 2. 获取用户所有心形墙项目
- **URL**: `GET /api/heart-wall/projects`
- **响应示例**:
```json
{
  "success": true,
  "message": "获取用户心形墙项目列表成功",
  "projects": [
    {
      "id": 1,
      "userId": 1,
      "projectName": "我们的回忆",
      "description": "记录我们的美好时光",
      "photoCount": 5,
      "maxPhotos": 40,
      "coverPhotoUrl": "https://example.com/cover.jpg",
      "isPublic": false,
      "createdAt": "2025-11-03T10:00:00.000+00:00",
      "updatedAt": "2025-11-03T10:00:00.000+00:00"
    }
  ]
}
```

### 3. 获取心形墙项目详情
- **URL**: `GET /api/heart-wall/projects/{projectId}`
- **响应示例**:
```json
{
  "success": true,
  "message": "获取心形墙项目详情成功",
  "project": {
    "id": 1,
    "userId": 1,
    "projectName": "我们的回忆",
    "description": "记录我们的美好时光",
    "photoCount": 5,
    "maxPhotos": 40,
    "coverPhotoUrl": "https://example.com/cover.jpg",
    "isPublic": false,
    "createdAt": "2025-11-03T10:00:00.000+00:00",
    "updatedAt": "2025-11-03T10:00:00.000+00:00"
  }
}
```

### 4. 更新心形墙项目
- **URL**: `PUT /api/heart-wall/projects/{projectId}`
- **请求参数**:
```json
{
  "projectName": "我们的甜蜜回忆",
  "description": "记录我们的甜蜜时光",
  "isPublic": true
}
```
- **响应示例**:
```json
{
  "success": true,
  "message": "心形墙项目更新成功",
  "project": {
    "id": 1,
    "userId": 1,
    "projectName": "我们的甜蜜回忆",
    "description": "记录我们的甜蜜时光",
    "photoCount": 5,
    "maxPhotos": 40,
    "coverPhotoUrl": "https://example.com/cover.jpg",
    "isPublic": true,
    "createdAt": "2025-11-03T10:00:00.000+00:00",
    "updatedAt": "2025-11-03T11:00:00.000+00:00"
  }
}
```

### 5. 删除心形墙项目
- **URL**: `DELETE /api/heart-wall/projects/{projectId}`
- **响应示例**:
```json
{
  "success": true,
  "message": "心形墙项目删除成功"
}
```

## 照片相关接口

### 1. 上传照片到心形墙
- **URL**: `POST /api/heart-wall/photos`
- **请求参数**:
```json
{
  "projectId": 1,
  "photoUrl": "https://example.com/photo.jpg",
  "thumbnailUrl": "https://example.com/thumb.jpg",
  "positionIndex": 1,
  "caption": "这是我们第一次约会",
  "takenDate": "2025-11-01"
}
```
- **响应示例**:
```json
{
  "success": true,
  "message": "照片上传成功",
  "photo": {
    "id": 1,
    "projectId": 1,
    "userId": 1,
    "photoUrl": "https://example.com/photo.jpg",
    "thumbnailUrl": "https://example.com/thumb.jpg",
    "positionIndex": 1,
    "caption": "这是我们第一次约会",
    "takenDate": "2025-11-01",
    "uploadedAt": "2025-11-03T10:30:00.000+00:00",
    "updatedAt": "2025-11-03T10:30:00.000+00:00"
  }
}
```

### 2. 获取心形墙项目中的所有照片
- **URL**: `GET /api/heart-wall/projects/{projectId}?action=photos[&page=1&pageSize=20]`
- **响应示例**:
```json
{
  "success": true,
  "message": "获取心形墙照片列表成功",
  "photos": [
    {
      "id": 1,
      "projectId": 1,
      "userId": 1,
      "photoUrl": "https://example.com/photo.jpg",
      "thumbnailUrl": "https://example.com/thumb.jpg",
      "positionIndex": 1,
      "caption": "这是我们第一次约会",
      "takenDate": "2025-11-01",
      "uploadedAt": "2025-11-03T10:30:00.000+00:00",
      "updatedAt": "2025-11-03T10:30:00.000+00:00"
    }
  ],
  "photoCount": 5
}
```

### 3. 获取下一个可用位置
- **URL**: `GET /api/heart-wall/next-position?projectId=1`
- **响应示例**:
```json
{
  "success": true,
  "message": "获取下一个可用位置成功",
  "nextPosition": 2
}
```

### 4. 更新照片信息
- **URL**: `PUT /api/heart-wall/photos/{photoId}`
- **请求参数**:
```json
{
  "photoUrl": "https://example.com/new-photo.jpg",
  "thumbnailUrl": "https://example.com/new-thumb.jpg",
  "positionIndex": 2,
  "caption": "这是我们第一次约会的更新版",
  "takenDate": "2025-11-01"
}
```
- **响应示例**:
```json
{
  "success": true,
  "message": "照片更新成功",
  "photo": {
    "id": 1,
    "projectId": 1,
    "userId": 1,
    "photoUrl": "https://example.com/new-photo.jpg",
    "thumbnailUrl": "https://example.com/new-thumb.jpg",
    "positionIndex": 2,
    "caption": "这是我们第一次约会的更新版",
    "takenDate": "2025-11-01",
    "uploadedAt": "2025-11-03T10:30:00.000+00:00",
    "updatedAt": "2025-11-03T11:30:00.000+00:00"
  }
}
```

### 5. 删除照片
- **URL**: `DELETE /api/heart-wall/photos/{photoId}`
- **响应示例**:
```json
{
  "success": true,
  "message": "照片删除成功"
}
```