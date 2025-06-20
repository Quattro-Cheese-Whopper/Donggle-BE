package com.donggle.ai.config;

import com.donggle.ai.tool.ApplicationTool;
import com.donggle.ai.tool.ClubSearchTool;
import com.donggle.ai.tool.NotificationTool;
import com.donggle.ai.tool.RecruitmentTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final ClubSearchTool clubSearchTool;
    private final RecruitmentTool recruitmentTool;
    private final ApplicationTool applicationTool;
    private final NotificationTool notificationTool;

    @Value("classpath:/prompts/system-template.st")
    private Resource systemTemplate;

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultTools(clubSearchTool, recruitmentTool, applicationTool, notificationTool)
                .build();
    }
}
