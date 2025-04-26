package com.mediconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AiServiceConfig {

    @Value("${mediconnect.ai.core-pool-size:2}")
    private int corePoolSize;

    @Value("${mediconnect.ai.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${mediconnect.ai.queue-capacity:100}")
    private int queueCapacity;

    @Value("${mediconnect.ai.model.version:1.0.0}")
    private String aiModelVersion;

    /**
     * Thread pool for asynchronous AI processing tasks
     */
    @Bean(name = "aiTaskExecutor")
    Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("AiTask-");
        executor.initialize();
        return executor;
    }

    /**
     * Configures AI model settings
     */
    @Bean
    AiModelConfig aiModelConfig() {
        return new AiModelConfig(aiModelVersion);
    }

    /**
     * AI model configuration class
     */
    public static class AiModelConfig {
        private final String version;

        public AiModelConfig(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}