package com.ftc.ftcli.infra;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-04-24 11:09:45
 * @describe 聊天记录Redis存储
 */
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    /**
     * Redis Key前缀
     */
    private final static String REDIS_KEY_PREFIX = "chat:memory:";

    /**
     * Redis模板
     */
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {

        //1.定义Redis Key
        String key = getRedisKey(memoryId);

        //2.获取Redis数据
        String json = stringRedisTemplate.opsForValue().get(key);

        //3.如果Redis数据为空，则返回空列表
        if (StrUtil.isBlank(json)) {
            return new ArrayList<>();
        }

        //4.获取Redis数据并转成ChatMessage列表,返回
        return messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {

        //1.messages转json
        String json = messagesToJson(messages);

        //2.定义Redis Key
        String key = getRedisKey(memoryId);

        //3.写入Redis
        stringRedisTemplate.opsForValue().set(key, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {

        //1.定义Redis Key
        String key = getRedisKey(memoryId);

        //2.删除Redis数据
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取Redis Key
     *
     * @param memoryId 存储ID
     * @return Redis Key
     */
    private static String getRedisKey(Object memoryId) {
        return REDIS_KEY_PREFIX + memoryId;
    }
}
