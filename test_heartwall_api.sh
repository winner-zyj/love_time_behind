#!/bin/bash

# 测试心形墙API接口
# Test heart wall API endpoints

echo "=== 测试心形墙API接口 ==="

# 请将下面的TOKEN替换为有效的用户token
TOKEN="your_valid_token_here"

# 测试获取项目列表
echo "1. 获取项目列表:"
curl -X GET "http://localhost:8080/lovetime/api/heart-wall/projects" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

echo -e "\n\n2. 测试完成"