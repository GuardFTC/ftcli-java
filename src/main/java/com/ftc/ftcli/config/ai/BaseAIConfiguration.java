package com.ftc.ftcli.config.ai;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-04-23 14:17:22
 * @describe 基础配置类
 */
@Slf4j
@Configuration
public class BaseAIConfiguration {

    @Bean
    public ChatModelListener chatModelListener() {
        return new ChatModelListener() {

            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.debug("onRequest(): {}", requestContext.chatRequest());
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.debug("onResponse(): {}", responseContext.chatResponse());
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.debug("onError(): {}", errorContext.error().getMessage());
            }
        };
    }
}
