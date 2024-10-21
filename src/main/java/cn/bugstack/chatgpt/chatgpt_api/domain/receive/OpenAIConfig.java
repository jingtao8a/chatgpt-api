package cn.bugstack.chatgpt.chatgpt_api.domain.receive;

import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatglm.session.OpenAiSessionFactory;
import cn.bugstack.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OpenAIConfig {
    /** 转发地址 */
    @Value("${chatglm.sdk.config.api-host}")
    private String apiHost;
    /** 可以申请 sk-*** */
    @Value("${chatglm.sdk.config.api-secret-key}")
    private String apiSecretKey;

    @Bean
    public OpenAiSession openAiSession() {
        log.info("apiHost: {}, apiSecretKey: {}", apiHost, apiSecretKey);
        cn.bugstack.chatglm.session.Configuration configuration = new cn.bugstack.chatglm.session.Configuration();
        configuration.setApiHost(apiHost);
        configuration.setApiSecretKey(apiSecretKey);
        configuration.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        OpenAiSession openAiSession = factory.openSession();
        log.info("开始 openAiSession");
        return openAiSession;
    }
}
