package cn.bugstack.chatgpt.chatgpt_api;

import cn.bugstack.chatgpt.chatgpt_api.domain.security.service.JwtUtil;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class ChatgptApiApplicationTests {

	@Test
	void contextLoads() {
		Logger logger = LoggerFactory.getLogger(ChatgptApiApplicationTests.class);
		logger.error("hello");
	}

	@Test
	public void test_jwt() {
		JwtUtil util = new JwtUtil("yuxintao", SignatureAlgorithm.HS256);
		Map<String, Object> map = new HashMap<>();
		map.put("username", "yuxintao");
		map.put("age", 22);
		String jwtToken = util.encode("yuxintao", 30000, map);
		if (util.isVerify(jwtToken)) {
			util.decode(jwtToken).forEach((key, value) -> {
				System.out.println(key + " : " + value);
			});
		}
	}
}
