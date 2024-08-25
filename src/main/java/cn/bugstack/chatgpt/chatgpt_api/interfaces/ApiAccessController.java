package cn.bugstack.chatgpt.chatgpt_api.interfaces;

import cn.hutool.jwt.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ApiAccessController {
    private Logger logger = LoggerFactory.getLogger(ApiAccessController.class);

    @RequestMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize(String username, String password) {
        Map<String, String> map = new HashMap<>();
        //模拟账号和密码校验
        if (!"jingtao8a".equals(username) || !"2001yuxintao".equals(password)) {
            map.put("msg", "用户名密码错误");
            return ResponseEntity.ok(map);
        }
        //校验通过生成token
        JWTUtil jwtUtil = new JWTUtil();
        Map<String, Object> chaim = new HashMap<>();
        chaim.put("username", username);
        String jwtToken = jwtUtil.createToken(chaim, password.getBytes());

        map.put("msg", "授权成功");
        map.put("token", jwtToken);
        //返回token码
        return ResponseEntity.ok(map);
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verify(String token) {
        logger.info("验证 token: {}", token);
        return ResponseEntity.status(HttpStatus.OK).body("verify success");
    }

    @GetMapping("/success")
    public String success() {
        return "test success by yxt";
    }
}
