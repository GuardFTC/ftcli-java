package com.ftc.ftcli.ai.store;

import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.entity.chat.ChatMemoryEntity;
import com.ftc.ftcli.infra.sqlite.ChatMemoryRepository;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-12 16:14:42
 * @describe 聊天记录SQLite存储
 */
@Component
@RequiredArgsConstructor
public class SqliteChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryRepository chatMemoryRepository;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {

        //1.查询聊天记录
        ChatMemoryEntity entity = chatMemoryRepository.findByMemoryId(memoryId.toString());

        //2.如果记录为空，返回空列表
        if (entity == null || StrUtil.isBlank(entity.getChatMessage())) {
            return new ArrayList<>();
        }

        //3.反序列化消息列表，返回
        return messagesFromJson(entity.getChatMessage());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {

        //1.序列化消息列表
        String json = messagesToJson(messages);

        //2.保存或更新
        chatMemoryRepository.saveOrUpdate(memoryId.toString(), json);
    }

    @Override
    public void deleteMessages(Object memoryId) {

        //1.删除聊天记录
        chatMemoryRepository.deleteByMemoryId(memoryId.toString());
    }
}
