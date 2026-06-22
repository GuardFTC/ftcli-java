package com.ftc.ftcli.common.util.doc.ingestor;

import com.ftc.ftcli.common.enums.doc.DocIngestorTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 15:15:29
 * @describe 文档切分器工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocIngestorFactory implements ApplicationContextAware {

    /**
     * 文档切分器Map
     */
    private static final Map<String, IIngestor> DOC_INGESTOR_MAP = new ConcurrentHashMap<>();

    /**
     * 获取文档切分器
     *
     * @param type 文档切分器类型
     * @return 文档切分器
     */
    public static IIngestor getDocIngestor(String type) {

        //1.获取文档切分器
        IIngestor docIngestor = DOC_INGESTOR_MAP.get(type);

        //2.如果为空，返回默认文档切分器
        if (null == docIngestor) {
            log.warn("[文档切分器工厂] 通过类型:[{}] 未获取到对应的文档切分器，返回通用文档切分器", type);
            docIngestor = DOC_INGESTOR_MAP.get(DocIngestorTypeEnum.UNIVERSAL.getType());
        }

        //3.不为空，返回特定的文档切分器
        return docIngestor;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        //1.获取全部实现类
        final Map<String, IIngestor> beansOfType = applicationContext.getBeansOfType(IIngestor.class);

        //2.循环
        for (String className : beansOfType.keySet()) {

            //3.获取实现类以及typeId集合
            final IIngestor docIngestor = beansOfType.get(className);

            //4.封装Map
            DOC_INGESTOR_MAP.put(docIngestor.getDocIngestorType(), docIngestor);
        }
    }
}
