package cn.bugstack.chatgpt.chatgpt_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ChatgptApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatgptApiApplication.class, args);
	}

}
