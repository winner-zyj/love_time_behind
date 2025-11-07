#!/bin/bash

# 测试获取相爱天数接口
# 需要先设置有效的用户token

echo "=== 测试获取相爱天数接口 ==="

# 请将下面的TOKEN替换为有效的用户token
TOKEN="your_valid_token_here"

# 测试获取相爱天数
echo "1. 获取相爱天数:"
curl -X GET "http://localhost:8080/lovetime/api/couple/love-days" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

echo -e "\n\n2. 测试完成"