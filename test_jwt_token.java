import com.abc.love_time.util.JwtUtil;

public class TestJwtToken {
    public static void main(String[] args) {
        // 测试JWT令牌生成和验证
        String openid = "testOpenid123";
        String sessionKey = "testSessionKey456";
        
        // 生成令牌
        String token = JwtUtil.generateToken(openid, sessionKey);
        System.out.println("Generated token: " + token);
        
        // 验证令牌
        boolean isValid = JwtUtil.validateToken(token);
        System.out.println("Token is valid: " + isValid);
        
        // 从令牌中提取openid
        if (isValid) {
            String extractedOpenid = JwtUtil.getOpenidFromToken(token);
            System.out.println("Extracted openid: " + extractedOpenid);
            System.out.println("Openid matches: " + openid.equals(extractedOpenid));
        }
    }
}