package com.ftc.ftcli.ai.tool;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.ai.tool.executor.ToolExecutorFactory;
import com.ftc.ftcli.ai.tool.provider.IToolProvider;
import com.ftc.ftcli.ai.tool.provider.ToolProviderFactory;
import com.ftc.ftcli.ai.tool.spec.ToolSpecBuilder;
import com.ftc.ftcli.ai.tool.spec.ToolSpecEntity;
import com.ftc.ftcli.infra.ToolSpecRepository;
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
     * 工具缓存 类型 -> (工具描述 -> 工具执行器)
     */
    private static final ConcurrentHashMap<String, Map<ToolSpecification, ToolExecutor>> TOOL_CACHE = new ConcurrentHashMap<>();

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

            //2.遍历工具提供者集合
            for (IToolProvider provider : ToolProviderFactory.getToolProviders()) {

                //3.条件不匹配则跳过
                if (!provider.isMatch(request)) {
                    continue;
                }

                //4.获取匹配的工具
                Map<ToolSpecification, ToolExecutor> tools = provider.getTools(TOOL_CACHE);

                //5.工具不为空则加入最终集合
                if (CollUtil.isNotEmpty(tools)) {
                    matchedTools.putAll(tools);
                }
            }

            //6.构建ToolProviderResult返回
            return ToolProviderResult.builder().addAll(matchedTools).build();
        };
    }

    @Override
    public void run(ApplicationArguments args) {

        //1.从数据库查询全部工具规格
        List<ToolSpecEntity> toolSpecEntities = toolSpecRepository.findAll();

        //2.遍历构建工具
        for (ToolSpecEntity entity : toolSpecEntities) {

            //3.构建ToolSpecification
            ToolSpecification spec = ToolSpecBuilder.buildToolSpecification(entity);

            //4.获取对应的执行器
            ToolExecutor executor = ToolExecutorFactory.getToolExecutor(entity.getName());

            //5.执行器不存在则跳过
            if (null == executor) {
                log.warn("[工具注册中心] 工具[{}]未找到对应执行器,跳过", entity.getName());
                continue;
            }

            //6.按类型分组缓存
            TOOL_CACHE.computeIfAbsent(entity.getType(), k -> new HashMap<>()).put(spec, executor);
        }

        //7.打印日志
        log.info("[工具注册中心] 工具加载完成,共加载[{}]种类型", TOOL_CACHE.size());
    }
}
