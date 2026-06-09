package com.ftc.ftcli.common.util.doc_loader;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 15:15:29
 * @describe 文档加载器工厂
 */
@Component
public class DocLoaderFactory implements ApplicationContextAware {

    /**
     * 文档加载器Map
     */
    private static final Map<DocLoaderEnum, IDocLoader> DOC_LOADER_MAP = new ConcurrentHashMap<>();

    /**
     * 获取文档加载器
     *
     * @param loaderEnum 文档加载器枚举
     * @return 文档加载器
     */
    public static IDocLoader getDocLoader(DocLoaderEnum loaderEnum) {

        //1.获取处理器
        IDocLoader docLoader = DOC_LOADER_MAP.get(loaderEnum);

        //2.判空
        if (null == docLoader) {
            throw new RuntimeException("文档加载器不存在:" + loaderEnum.getType());
        }

        //3.返回
        return docLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        //1.获取全部实现类
        final Map<String, IDocLoader> beansOfType = applicationContext.getBeansOfType(IDocLoader.class);

        //2.循环
        for (String className : beansOfType.keySet()) {

            //3.获取实现类以及typeId集合
            final IDocLoader docLoader = beansOfType.get(className);

            //4.封装Map
            DOC_LOADER_MAP.put(docLoader.getType(), docLoader);
        }
    }
}
