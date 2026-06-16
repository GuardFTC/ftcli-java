package com.ftc.ftcli.ai.service;

import cn.hutool.core.io.resource.ResourceUtil;
import com.ftc.ftcli.ai.store.SqliteChatMemoryStore;
import com.ftc.ftcli.properties.chat.ChatMemoryProperties;
import com.ftc.ftcli.service.AISkillService;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.skills.Skills;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-16 17:00:00
 * @describe AI服务持有者，支持Skill动态热加载
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class AiServiceHolder {

    private final ChatModel model;

    private final StreamingChatModel streamingModel;

    private final SqliteChatMemoryStore chatMemoryStore;

    private final ToolProvider toolProvider;

    private final QueryTransformer queryTransformer;

    private final QueryRouter webAiQueryRouter;

    private final QueryRouter localAiQueryRouter;

    private final ContentInjector contentInjector;

    private final ChatMemoryProperties chatMemoryProperties;

    private final AISkillService aiSkillService;

    @Getter
    private volatile WebAiService webAiService;

    @Getter
    private volatile LocalAiService localAiService;

    @PostConstruct
    public void init() {
        buildAiService();
    }

    /**
     * 构建AIService
     */
    public synchronized void buildAiService() {

        //1.加载Skills
        Skills skills = aiSkillService.loadSkills();

        //2.构建WebAiService
        String webSystemMessage = buildSystemMessage("prompt/web-service.md", skills);
        this.webAiService = AiServices.builder(WebAiService.class)
                .chatModel(model)
                .streamingChatModel(streamingModel)
                .systemMessageProvider(memoryId -> webSystemMessage)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .toolProviders(toolProvider, skills.toolProvider())
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .queryTransformer(queryTransformer)
                        .queryRouter(webAiQueryRouter)
                        .build())
                .build();

        //3.构建LocalAiService
        String localSystemMessage = buildSystemMessage("prompt/local-service.md", skills);
        this.localAiService = AiServices.builder(LocalAiService.class)
                .chatModel(model)
                .streamingChatModel(streamingModel)
                .systemMessageProvider(memoryId -> localSystemMessage)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .toolProviders(toolProvider, skills.toolProvider())
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .queryTransformer(queryTransformer)
                        .queryRouter(localAiQueryRouter)
                        .contentInjector(contentInjector)
                        .build())
                .build();

        //4.打印日志
        log.info("[AiServiceHolder] AI服务构建完成");
    }

    /**
     * 加载 prompt 文件并拼接 Skills 清单
     */
    private String buildSystemMessage(String promptResource, Skills skills) {

        //1.读取 prompt 文件内容
        String promptContent = ResourceUtil.readUtf8Str(promptResource);

        //2.获取 Skills 清单
        String skillsList = skills.formatAvailableSkills();

        //3.拼接并返回
        return promptContent + "\n\n---\n\n"
                + "你拥有以下可按需激活的技能,当用户请求与某个技能相关时,先调用 activate_skill 工具激活它:\n"
                + skillsList;
    }
}
