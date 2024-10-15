package cn.bugstack.chatgpt.chatgpt_api.domain.security.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtUtil {
    //创建默认的密钥和算法，供无参的构造方法使用
    private static final String defaultBase64EncodedSecretKey = "yuxintao";
    private static final SignatureAlgorithm defaultSignatureAlgorithm = SignatureAlgorithm.HS256;
    public JwtUtil() {
        this(defaultBase64EncodedSecretKey, defaultSignatureAlgorithm);
    }

    private String base64EncodedSecretKey;
    private SignatureAlgorithm signatureAlgorithm;
    public JwtUtil(String secretKey, SignatureAlgorithm signatureAlgorithm) {
        this.base64EncodedSecretKey = secretKey;
        this.signatureAlgorithm = signatureAlgorithm;
    }
    /*
     *  这里就是产生jwt字符串的地方
     *  jwt字符串包括三个部分
     *  1. header
     *      -当前字符串的类型，一般是“JWT”
     *      -哪种算法加密，“HS256" 或者其它加密算法
     *      所以一般都是固定的，没有什么变化
     *  2. payload
     *      一般有四个常见的标准字段（下面有）
     *      iat：签发时间，也就是这个jwt什么时候生成的
     *      jti：JWT的唯一标识
     *      iss：签发人， 一般都是username或者userId
     *      exp: 过期时间
     */

    public String encode(String issuer, long ttlMillis, Map<String, Object> claims) {
        //iss签发人，ttlMillis生存时间，claims是存储在jwt中的一些非隐私信息
        if (claims == null) {
            claims = new HashMap<>();
        }
        long nowMillis = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                //载荷部分
                .setClaims(claims)
                //签发时间
                .setIssuedAt(new Date(nowMillis))
                //这个是JWT的唯一标识，一般设置成唯一的，这个方法可以生成唯一标识
                .setId(UUID.randomUUID().toString())
                //签发人，也就是JWT是给谁的(逻辑上一般都是username或者userId)
                .setSubject(issuer)
                .signWith(signatureAlgorithm, base64EncodedSecretKey);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    public Claims decode(String jwtToken) {
        return Jwts.parser()
                //设置签名的密钥
                .setSigningKey(base64EncodedSecretKey)
                //设置需要解析的jwt
                .parseClaimsJws(jwtToken)
                .getBody();
    }
    public boolean isVerify(String jwtToken) {
        //这是官方的校验规则，这里只写了一个"校验算法"，可以自己加
        Algorithm algorithm = null;
        switch(signatureAlgorithm) {
            case HS256:
                algorithm = Algorithm.HMAC256(Base64.decodeBase64(base64EncodedSecretKey));
                break;
            default:
                throw new RuntimeException("不支持该算法");
        }
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(jwtToken);
        //校验不通过会抛出异常
        //判断合法标准：1.头部和载荷部分没有篡改过 2.没有过期
        return true;
    }


}
