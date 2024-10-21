package cn.bugstack.chatgpt.chatgpt_api;

import cn.bugstack.chatglm.model.ChatCompletionRequest;
import cn.bugstack.chatglm.model.ChatCompletionResponse;
import cn.bugstack.chatglm.model.Model;
import cn.bugstack.chatglm.model.Role;
import cn.bugstack.chatglm.session.Configuration;
import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatglm.session.OpenAiSessionFactory;
import cn.bugstack.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import cn.bugstack.chatgpt.chatgpt_api.domain.security.service.JwtUtil;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@Slf4j
class ChatgptApiApplicationTests {
	@Resource
	private OpenAiSession openAiSession;
	@Value("${chatglm.sdk.config.enable}")
	private boolean enable;
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

	@Test
	public void testChatGLM() {
		String content = "用cpp写一个冒泡排序";
		StringBuilder stringBuilder = new StringBuilder();
		CountDownLatch countDownLatch = new CountDownLatch(1);
		// 入参；模型、请求信息
		ChatCompletionRequest request = new ChatCompletionRequest();
		request.setModel(Model.GLM_4); // GLM_3_5_TURBO、GLM_4
		request.setIsCompatible(enable);
		request.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
			private static final long serialVersionUID = -7988151926241837899L;
			{
				add(ChatCompletionRequest.Prompt.builder()
						.role(Role.user.getCode())
						.content(content)
						.build());
			}
		});
		// 请求
		try {
			openAiSession.completions(request, new EventSourceListener() {
				@Override
				public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
					if ("[DONE]".equals(data)) {
						log.info("[输出结束] Tokens {}", JSON.toJSONString(data));
						return;
					}
					ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
					stringBuilder.append(response.getData());
					log.info("测试结果：{}", JSON.toJSONString(response));
				}
				@Override
				public void onClosed(EventSource eventSource) {
					log.info("对话完成");
					countDownLatch.countDown();
				}
				@Override
				public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
					log.error("对话失败", t);
					countDownLatch.countDown();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// 等待
		try {
			countDownLatch.await();
			log.info("over {}", stringBuilder.toString());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void test_completions_new() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setApiHost("https://open.bigmodel.cn/");
		configuration.setApiSecretKey("37b5e188bca26dcc4535afc4f91c1132.nk9J7UV04PuFQz8v");
		configuration.setLevel(HttpLoggingInterceptor.Level.BODY);
		// 2. 会话工厂
		OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
		// 3. 开启会话
		OpenAiSession openAiSession = factory.openSession();

		CountDownLatch countDownLatch = new CountDownLatch(1);

		// 入参；模型、请求信息
		ChatCompletionRequest request = new ChatCompletionRequest();
		request.setModel(Model.GLM_4); // GLM_3_5_TURBO、GLM_4
		request.setIsCompatible(false);
//        // 24年1月发布的 glm-3-turbo、glm-4 支持函数、知识库、联网功能
//        request.setTools(new ArrayList<ChatCompletionRequest.Tool>() {
//            private static final long serialVersionUID = -7988151926241837899L;
//
//            {
//                add(ChatCompletionRequest.Tool.builder()
//                        .type(ChatCompletionRequest.Tool.Type.web_search)
//                        .webSearch(ChatCompletionRequest.Tool.WebSearch.builder().enable(true).searchQuery("小傅哥").build())
//                        .build());
//            }
//        });
		request.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
			private static final long serialVersionUID = -7988151926241837899L;

			{
				add(ChatCompletionRequest.Prompt.builder()
						.role(Role.user.getCode())
						.content("用cpp写一个冒泡排序")
						.build());
			}
		});

		// 请求
		openAiSession.completions(request, new EventSourceListener() {
			@Override
			public void onEvent(EventSource eventSource, @org.jetbrains.annotations.Nullable String id, @org.jetbrains.annotations.Nullable String type, String data) {
				if ("[DONE]".equals(data)) {
					log.info("[输出结束] Tokens {}", JSON.toJSONString(data));
					return;
				}

				ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);
				log.info("测试结果：{}", JSON.toJSONString(response));
			}

			@Override
			public void onClosed(EventSource eventSource) {
				log.info("对话完成");
				countDownLatch.countDown();
			}

			@Override
			public void onFailure(EventSource eventSource, @org.jetbrains.annotations.Nullable Throwable t, @org.jetbrains.annotations.Nullable Response response) {
				log.error("对话失败", t);
				countDownLatch.countDown();
			}
		});

		// 等待
		countDownLatch.await();
	}
}
