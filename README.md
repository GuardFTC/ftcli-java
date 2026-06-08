# ftcli

> FTC 自研 AI 命令行助手后端服务 —— 基于 LangChain4j 的大模型对话、RAG 知识库问答与工具调用平台。

## 目录

- [项目简介](#项目简介)
- [核心特性](#核心特性)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API 接口](#api-接口)
- [内置工具](#内置工具)
- [扩展开发](#扩展开发)
- [项目结构](#项目结构)
- [注意事项](#注意事项)

---

## 项目简介

**ftcli** 是一个基于 Spring Boot 的 AI 助手服务，为命令行客户端或 Web 前端提供统一的 AI 能力接入层。项目以 [LangChain4j](https://github.com/langchain4j/langchain4j) 为核心，整合了：

- **大模型对话**：支持同步与 SSE 流式响应
- **RAG 知识库**：基于本地 Markdown 文档的向量检索问答
- **联网搜索**：通过 Tavily 获取实时信息
- **Function Calling**：可扩展的 AI 工具调用框架

服务默认监听 **6680** 端口，同时提供 REST API 与简易静态管理页面。

---

## 核心特性

### 双模式 AI 问答

通过 `isLocal` 参数在两种助手模式间切换：

| 模式 | 服务 | 说明 |
|------|------|------|
| 本地模式 (`isLocal=true`) | `LocalAiService` | 从 Chroma 向量库检索相关文档片段，优先基于本地知识库回答 |
| 联网模式 (`isLocal=false`) | `WebAiService` | 由 LLM 智能路由，按需调用 Tavily 联网搜索获取实时信息 |

两种模式均支持：

- 多轮对话（Redis 持久化会话记忆）
- Token 窗口管理（默认上限 95000 tokens）
- 动态工具调用

### 知识库管理

- 支持上传 **单个 Markdown 文件** 或 **整个目录**（递归扫描）
- 文档自动切分、向量化后写入 Chroma
- 基于文件名 MD5 去重，基于内容 MD5 检测变更并自动更新向量
- SQLite 作为文档元数据的唯一事实源，向量写入失败时可重试自愈

### 可扩展工具系统

- 工具描述（名称、描述、参数）存储在 SQLite，支持运行时 CRUD
- 工具执行器通过 Spring Bean 自动注册
- 启动时由 `ToolRegistry` 将描述与执行器绑定，运行时由 `ToolProvider` 动态匹配

### 开箱即用的管理页面

| 页面 | 地址 | 功能 |
|------|------|------|
| 文档管理 | `http://localhost:6680/docs.html` | 查看、上传、删除知识库文档 |
| 工具管理 | `http://localhost:6680/tools.html` | 查看、新增、编辑、删除 AI 工具 |

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.4.5 |
| AI 框架 | LangChain4j | 1.12.2-beta22 |
| 对话模型 | DeepSeek Chat | OpenAI 兼容协议 |
| Embedding 模型 | 智谱 AI embedding-3 | — |
| 向量数据库 | Chroma | V2 API |
| 联网搜索 | Tavily | — |
| 会话存储 | Redis | — |
| 元数据存储 | SQLite | 3.49.1 |
| API 文档 | SpringDoc OpenAPI | 2.8.8 |
| 工具库 | Hutool、Fastjson2 | — |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端 / 静态页面                          │
│              (命令行工具 / docs.html / tools.html)                │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP REST / SSE
┌────────────────────────────▼────────────────────────────────────┐
│                      Spring Boot Controllers                     │
│         AIChatController │ AIEmbeddingController │ AIToolController│
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
  AIChatService      AIEmbeddingService      AIToolService
        │                    │                    │
        ▼                    ▼                    ▼
 ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
 │ LocalAiSvc  │      │  Ingestor   │      │ ToolSpec    │
 │ WebAiSvc    │      │  + Chroma   │      │ Repository  │
 └──────┬──────┘      └──────┬──────┘      └─────────────┘
        │                    │
        ├──────────┬─────────┤
        ▼          ▼         ▼
   DeepSeek    Chroma    Tavily
   Chat API   向量库    Web Search
        │
        ├──────── Redis（会话记忆）
        └──────── ToolRegistry（工具调用）
```

### 关键设计

1. **RAG 流水线**：文档加载 → Markdown 解析 → 递归切分 → 智谱 Embedding → Chroma 存储 → 检索增强生成
2. **会话记忆**：`TokenWindowChatMemory` + `RedisChatMemoryStore`，按 `chatId` 隔离多轮上下文
3. **工具注册**：SQLite 存描述，Java 代码存执行逻辑，启动时绑定，解耦配置与实现
4. **数据一致性**：向量写入成功后才更新 SQLite 记录；上传前按 `file_name_md5` 清理残留向量，保证幂等

---

## 环境要求

运行本项目前，请确保以下服务可用：

| 服务 | 默认地址 | 用途 |
|------|----------|------|
| Redis | `localhost:6379` | 会话记忆存储 |
| Chroma | `http://localhost:8000` | 向量数据库 |
| DeepSeek API | `https://api.deepseek.com` | 对话大模型 |
| 智谱 AI API | — | Embedding 向量化 |
| Tavily API | — | 联网搜索（Web 模式） |

### 启动 Chroma（Docker 示例）

```bash
docker run -d --name chroma -p 8000:8000 chromadb/chroma
```

### 启动 Redis（Docker 示例）

```bash
docker run -d --name redis -p 6379:6379 redis:7
```

---

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd ftcli
```

### 2. 配置应用

编辑 `src/main/resources/application.yaml`，填入各服务的 API Key 和连接信息（详见[配置说明](#配置说明)）。

> 建议将敏感信息通过环境变量注入，不要将真实 API Key 提交到版本库。

### 3. 启动服务

**方式一：Maven Wrapper（推荐）**

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

**方式二：打包后运行**

```bash
.\mvnw.cmd clean package -DskipTests
java -jar target/ftcli-0.0.1-SNAPSHOT.jar
```

### 4. 验证服务

- API 文档：http://localhost:6680/swagger-ui/index.html
- 文档管理：http://localhost:6680/docs.html
- 工具管理：http://localhost:6680/tools.html

### 5. 快速体验

**获取会话 ID：**

```bash
curl http://localhost:6680/api/rest/v1/ai/chatId
```

**本地知识库问答：**

```bash
curl -X POST http://localhost:6680/api/rest/v1/ai/chat \
  -H "Content-Type: application/json" \
  -d "{\"isLocal\": true, \"chatId\": \"your-chat-id\", \"userMessage\": \"你好\"}"
```

**流式对话：**

```bash
curl -X POST http://localhost:6680/api/rest/v1/ai/chat/stream \
  -H "Content-Type: application/json" \
  -d "{\"isLocal\": false, \"chatId\": \"your-chat-id\", \"userMessage\": \"今天有什么科技新闻？\"}"
```

**上传知识库文档：**

```bash
curl -X POST http://localhost:6680/api/rest/v1/ai/embedding/docs \
  -H "Content-Type: application/json" \
  -d "{\"path\": \"C:/path/to/your/docs\"}"
```

---

## 配置说明

主配置文件：`src/main/resources/application.yaml`

### 服务端口

```yaml
server:
  port: 6680
```

### Redis

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 1
```

### SQLite

```yaml
spring:
  datasource:
    url: jdbc:sqlite:C:/path/to/ftcli/data/ftcli.db
    driver-class-name: org.sqlite.JDBC
```

数据库在应用启动时由 `SqliteInitializer` 自动建表并初始化内置工具数据。

### 对话模型（DeepSeek）

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
      temperature: 0.7
      timeout: 30s
    streaming-chat-model:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
```

### Embedding 与向量库

```yaml
ai:
  embedding:
    model:
      zhipu:
        api-key: ${ZHIPU_API_KEY}
        model-name: embedding-3
    store:
      chroma:
        url: http://localhost:8000
        tenant: langchain4j
        database: langchain4j_db
        collection: langchain4j_coll
```

### RAG 切分与联网搜索

```yaml
ai:
  chat-memory:
    max-tokens: 95000
    token-estimator-model: gpt-4o
  rag:
    ingestor:
      max-segment-size: 1500    # 文档切分最大 token 数
      overlap: 200              # 切分重叠 token 数
    web-search:
      api-key: ${TAVILY_API_KEY}
```

---

## API 接口

所有接口统一返回 `RestfulResult<T>` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### AI 聊天 `/api/rest/v1/ai`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/chatId` | 生成新的会话 ID |
| POST | `/chat` | 同步对话 |
| POST | `/chat/stream` | SSE 流式对话 |

**请求体 `ChatPayload`：**

```json
{
  "isLocal": true,
  "chatId": "会话ID",
  "userMessage": "用户消息"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `isLocal` | Boolean | `true` = 本地知识库模式，`false` = 联网搜索模式 |
| `chatId` | String | 会话标识，用于关联多轮对话上下文 |
| `userMessage` | String | 用户输入 |

### 知识库管理 `/api/rest/v1/ai/embedding`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/docs` | 查询全部已录入文档 |
| POST | `/docs` | 上传文档（文件或目录路径） |
| DELETE | `/docs/{id}` | 按 ID 删除文档及对应向量 |

**上传请求体：**

```json
{
  "path": "C:/docs/my-project"
}
```

**上传响应 `EmbeddingFileUploadResult`：**

```json
{
  "newFiles": ["新增文件完整路径列表"],
  "updateFiles": ["内容变更后更新的文件路径列表"]
}
```

### 工具管理 `/api/rest/v1/ai/tools`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 查询全部工具 |
| POST | `/` | 新增工具 |
| PUT | `/` | 更新工具 |
| DELETE | `/{name}` | 按名称删除工具 |

**工具实体 `ToolSpecEntity`：**

```json
{
  "name": "getNowTime",
  "description": "获取当前时间",
  "type": "date",
  "params": [
    {
      "name": "unit",
      "description": "时间单位",
      "required": false,
      "type": "ENUMS",
      "enumValues": ["HOUR", "MINUTE", "SECOND"]
    }
  ]
}
```

---

## 内置工具

应用启动时通过 `sql/data.sql` 预置以下工具：

| 工具名 | 类型 | 描述 |
|--------|------|------|
| `getNowTime` | `date` | 获取当前时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `getRemainingTimeToDayEnd` | `date` | 计算当前时间距离当天结束的剩余时间 |

此外，代码中已实现但需通过 API 或数据库手动注册的系统工具：

| 工具名 | 类型 | 描述 |
|--------|------|------|
| `openFileOrDirectory` | `system` | 使用系统默认程序打开指定文件或目录（支持 Windows / macOS / Linux） |

### 工具类型

| 类型值 | 说明 | Provider |
|--------|------|----------|
| `date` | 日期时间类工具 | `DateToolProvider` |
| `system` | 系统操作类工具 | `SystemToolProvider` |
| `memory_id` | 按会话隔离的用户工具 | 预留扩展 |

---

## 扩展开发

### 新增 AI 工具

扩展工具需要 **两步**：实现执行器 + 注册工具描述。

#### 第一步：实现执行器

创建类实现 `IToolExecutor` 接口，并注册为 Spring Bean：

```java
@Component
public class MyToolExecutor implements IToolExecutor {

    @Override
    public String getName() {
        return "myTool";  // 必须与数据库中的工具名一致
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {
            Map<String, Object> args = JSON.parseObject(toolExecutionRequest.arguments());
            // 执行业务逻辑
            return "执行结果";
        };
    }
}
```

`ToolExecutorFactory` 会在启动时自动扫描所有 `IToolExecutor` 实现并注册。

#### 第二步：注册工具描述

通过 API 或直接向 SQLite 插入工具规格：

```bash
curl -X POST http://localhost:6680/api/rest/v1/ai/tools \
  -H "Content-Type: application/json" \
  -d '{
    "name": "myTool",
    "description": "我的自定义工具",
    "type": "system",
    "params": [
      {
        "name": "input",
        "description": "输入参数",
        "required": true,
        "type": "STRING"
      }
    ]
  }'
```

> 新增工具描述后需**重启应用**，`ToolRegistry` 才会重新加载并绑定执行器。

#### 参数类型

| 类型值 | 说明 |
|--------|------|
| `STRING` | 字符串 |
| `INTEGER` | 整数 |
| `NUMBER` | 浮点数 |
| `BOOLEAN` | 布尔值 |
| `ENUMS` | 枚举（需配合 `enumValues`） |

### 自定义系统提示词

提示词文件位于 `src/main/resources/prompt/`：

| 文件 | 对应服务 |
|------|----------|
| `local-service.markdown` | `LocalAiService`（本地 RAG 模式） |
| `web-service.markdown` | `WebAiService`（联网搜索模式） |

修改后重启服务即可生效。

### 新增工具 Provider

当工具类型增多时，可创建新的 `IToolProvider` 实现，在 `isMatch()` 中按用户意图过滤工具，避免向模型注入过多工具描述：

```java
@Component
public class MyToolProvider implements IToolProvider {

    @Override
    public boolean isMatch(ToolProviderRequest request) {
        // 根据用户消息判断是否提供此类工具
        return true;
    }

    @Override
    public Map<ToolSpecification, ToolExecutor> getTools(
            Map<String, Map<ToolSpecification, ToolExecutor>> cache) {
        return cache.get("my_type");
    }
}
```

---

## 项目结构

```
ftcli/
├── pom.xml                              # Maven 依赖与构建配置
├── mvnw / mvnw.cmd                      # Maven Wrapper
├── data/
│   └── ftcli.db                         # SQLite 数据库（运行时生成）
└── src/
    ├── main/
    │   ├── java/com/ftc/ftcli/
    │   │   ├── FtcliApplication.java    # 启动类
    │   │   ├── controller/              # REST 控制器
    │   │   │   ├── AIChatController.java
    │   │   │   ├── AIEmbeddingController.java
    │   │   │   └── AIToolController.java
    │   │   ├── service/                 # 业务服务层
    │   │   │   ├── AiChatService.java
    │   │   │   ├── AIEmbeddingService.java
    │   │   │   ├── AIToolService.java
    │   │   │   └── impl/
    │   │   ├── ai/
    │   │   │   ├── assistant/           # LangChain4j AI 服务接口
    │   │   │   │   ├── LocalAiService.java
    │   │   │   │   └── WebAiService.java
    │   │   │   └── tool/                # 工具调用框架
    │   │   │       ├── ToolRegistry.java
    │   │   │       ├── executor/        # 工具执行器
    │   │   │       ├── provider/        # 工具提供者
    │   │   │       └── spec/            # 工具规格构建
    │   │   ├── config/
    │   │   │   ├── ai/                  # AI、RAG、Embedding 配置
    │   │   │   ├── redis/               # Redis 配置
    │   │   │   └── sqlite/              # SQLite 初始化
    │   │   ├── infra/
    │   │   │   ├── redis/               # Redis 会话存储
    │   │   │   └── sqlite/              # SQLite 仓储
    │   │   ├── entity/                  # 请求/响应实体
    │   │   ├── properties/              # 配置属性类
    │   │   └── common/                  # 枚举、工具类
    │   └── resources/
    │       ├── application.yaml         # 应用配置
    │       ├── prompt/                  # AI 系统提示词
    │       ├── sql/                     # 数据库 Schema 与初始数据
    │       └── static/                  # 静态管理页面
    │           ├── docs.html
    │           └── tools.html
    └── test/
        └── java/com/ftc/ftcli/
            └── TestEveryThing.java
```

---

## 注意事项

1. **API Key 安全**：`application.yaml` 中包含多个第三方 API Key，生产环境请使用环境变量或密钥管理服务，避免泄露。
2. **SQLite 路径**：配置中的 SQLite 路径需使用绝对路径，并根据部署环境调整。
3. **文档格式**：知识库目前仅支持 **Markdown** 文件（`.md` / `.markdown`）。
4. **工具热更新**：通过 API 增删改工具描述后，需要重启应用才能生效（`ToolRegistry` 在启动时加载）。
5. **外部依赖**：Redis 和 Chroma 不可用时，对应功能（会话记忆 / 知识库）将无法正常工作。
6. **Chroma 版本**：项目使用 Chroma V2 API（`ChromaApiVersion.V2`），请确保 Chroma 服务版本兼容。

---

## 作者

冯铁城 — 17615007230@163.com

## 许可证

本项目为内部工具，暂未指定开源许可证。
