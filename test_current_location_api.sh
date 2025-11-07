#!/bin/bash

# 测试获取当前位置接口
# Test current location API

echo "=== 测试获取当前位置接口 ==="

# 请将下面的TOKEN替换为有效的用户token
TOKEN="your_valid_token_here"

# 测试获取当前位置
echo "1. 获取当前位置:"
curl -X GET "http://localhost:8080/lovetime/api/trajectory/location/current" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

echo -e "\n\n2. 测试完成"