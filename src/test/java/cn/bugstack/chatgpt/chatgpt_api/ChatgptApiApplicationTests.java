package cn.bugstack.chatgpt.chatgpt_api;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatgptApiApplicationTests {

	@Test
	void contextLoads() {
		Logger logger = LoggerFactory.getLogger(ChatgptApiApplicationTests.class);
		logger.error("hello");
	}

}
