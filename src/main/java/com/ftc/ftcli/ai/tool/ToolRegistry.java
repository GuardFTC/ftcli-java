package com.ftc.ftcli.ai.tool;

import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import com.ftc.ftcli.ai.tool.executor.ToolExecutorFactory;
import com.ftc.ftcli.ai.tool.spec.ToolSpecBuilder;
import com.ftc.ftcli.ai.tool.spec.ToolSpecEntity;
import com.ftc.ftcli.common.util.ai.AiTraceLog;
import com.ftc.ftcli.infra.sqlite.ToolSpecRepository;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 15:17:56
 * @describe AI工具注册中心 启动时从数据库加载工具描述并与执行器绑定 对外提供ToolProvider
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class ToolRegistry implements ApplicationRunner {

    private final ToolSpecRepository toolSpecRepository;

    /**
     * 工具缓存 工具描述 -> 工具执行器提供者
     */
    private static final ConcurrentHashMap<ToolSpecification, IToolExecutor> TOOL_CACHE = new ConcurrentHashMap<>();

    /**
     * 注册ToolProvider Bean 根据用户消息动态匹配工具
     *
     * @return ToolProvider
     */
    @Bean
    public ToolProvider toolProvider() {
        return (request) -> {

            //1.定义最终工具集合
            Map<ToolSpecification, ToolExecutor> matchedTools = new HashMap<>();

            //2.遍历工具缓存
            for (ToolSpecification toolSpec : TOOL_CACHE.keySet()) {

                //3.获取工具执行器
                IToolExecutor toolExecutor = TOOL_CACHE.get(toolSpec);

                //4.判断是否匹配
                if (!toolExecutor.isMatch(request)) {
                    continue;
                }

                //5.如果匹配，设置Trace执行器，添加日志
                ToolExecutor tracedExecutor = (toolExecutionRequest, memoryId) -> {

                    //6.打印调用参数
                    AiTraceLog.logToolCall(toolExecutionRequest.name(), toolExecutionRequest.arguments());

                    //7.执行工具
                    String result = toolExecutor.getToolExecutor().execute(toolExecutionRequest, memoryId);

                    //8.打印返回结果
                    AiTraceLog.logToolResult(toolExecutionRequest.name(), result);

                    //9.返回结果
                    return result;
                };

                //10.如果匹配，则加入最终集合
                matchedTools.put(toolSpec, tracedExecutor);
            }

            //11.构建ToolProviderResult返回
            return ToolProviderResult.builder().addAll(matchedTools).build();
        };
    }

    @Override
    public void run(ApplicationArguments args) {

        //1.从数据库查询全部工具描述
        List<ToolSpecEntity> toolSpecEntities = toolSpecRepository.findAll();

        //2.清空工具缓存
        TOOL_CACHE.clear();

        //3.加载工具缓存
        loadToolCache(toolSpecEntities);
    }

    /**
     * 加载工具缓存
     */
    public static void loadToolCache(List<ToolSpecEntity> toolSpecEntities) {

        //1.定义加载工具总数
        int toolCount = toolSpecEntities.size();

        //2.遍历构建工具
        for (ToolSpecEntity entity : toolSpecEntities) {

            //3.构建ToolSpecification
            ToolSpecification toolSpec = ToolSpecBuilder.buildToolSpecification(entity);

            //4.获取对应的执行器
            IToolExecutor toolExecutor = ToolExecutorFactory.getIToolExecutor(entity.getName());

            //5.执行器不存在则跳过
            if (null == toolExecutor) {
                toolCount--;
                log.warn("[工具注册中心] 工具[{}]未找到对应执行器,跳过", entity.getName());
                continue;
            }

            //6.按类型分组缓存
            TOOL_CACHE.put(toolSpec, toolExecutor);
        }

        //7.打印日志
        log.info("[工具注册中心] 工具加载完成,共[{}]个工具", toolCount);
    }
}
