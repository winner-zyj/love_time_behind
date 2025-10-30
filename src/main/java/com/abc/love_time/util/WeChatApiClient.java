package com.abc.love_time.util;

import com.abc.love_time.dto.WeChatSession;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 微信API客户端
 */
public class WeChatApiClient {
    // 微信小程序配置（生产环境应该配置在配置文件中）
    private static final String APP_ID = "wx236b5c878c81d830";  // 请替换为你的AppID
    private static final String APP_SECRET = "c7b707e3d0912276344bc77e881337ef";  // 请替换为你的AppSecret
    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private static final Gson gson = new Gson();

    /**
     * 通过code获取openid和session_key
     * @param code 登录凭证
     * @return 微信session信息
     * @throws Exception 调用异常
     */
    public static WeChatSession getSessionByCode(String code) throws Exception {
        // 构建请求URL
        String urlString = String.format(
                "%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                JSCODE2SESSION_URL, APP_ID, APP_SECRET, code
        );
        
        System.out.println("[微信API] 请求URL: " + urlString.replace(APP_SECRET, "***"));

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        System.out.println("[微信API] HTTP响应状态码: " + responseCode);
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            System.out.println("[微信API] 响应数据: " + response.toString());

            // 解析响应
            WeChatSession session = gson.fromJson(response.toString(), WeChatSession.class);
            
            // 检查是否有错误
            if (session.getErrcode() != null && session.getErrcode() != 0) {
                System.err.println("[微信API] 错误码: " + session.getErrcode() + ", 错误信息: " + session.getErrmsg());
                throw new Exception("微信API调用失败: " + session.getErrmsg());
            }
            
            System.out.println("[微信API] 成功获取openid");
            return session;
        } else {
            System.err.println("[微信API] HTTP请求失败，状态码: " + responseCode);
            throw new Exception("HTTP请求失败，状态码: " + responseCode);
        }
    }
}
