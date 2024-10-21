package cn.bugstack.chatgpt.chatgpt_api.domain.receive;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;
@Configuration
@Slf4j
public class ThreadPoolConfig {
//    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);
    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize;
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize;
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;
    @Value("${async.executor.thread.name_prefix}")
    private String namePrefix;
    @Value("${async.executor.thread.keep_alive_seconds}")
    private int keepAliveSeconds;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 设置队列容量
        executor.setQueueCapacity(queueCapacity);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 设置默认线程名称
        executor.setThreadNamePrefix(namePrefix);
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        log.info("创建一个线程池 corePoolSize is [" + corePoolSize + "] maxPoolSize is [" + maxPoolSize + "] queueCapacity is [" + queueCapacity +
                "] keepAliveSeconds is [" + keepAliveSeconds + "] namePrefix is [" + namePrefix + "].");
        return executor;
    }
}
