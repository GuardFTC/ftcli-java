# ftcli

> FTC 个人 AI 命令行助手后端服务 —— 基于 LangChain4j 的大模型对话、RAG 知识库问答与工具调用平台。

## 项目简介

**ftcli** 是一个基于 Spring Boot 的 AI 助手服务，为 Go 编写的命令行客户端（[go-ftc-console](../../../go/src/go-ftc-console)）提供 AI 能力后端。项目以 [LangChain4j](https://github.com/langchain4j/langchain4j) 为核心，整合了：

- **大模型对话**：支持同步与 SSE 流式响应（DeepSeek Chat）
- **RAG 知识库**：支持本地文件、URL、GitHub 链接多来源文档的向量检索问答
- **联网搜索**：通过 Tavily 获取实时信息
- **Function Calling**：数据库驱动的动态工具调用框架

服务默认监听 `6680` 端口，同时提供 REST API 与静态管理页面。

---

## 核心特性

### 双模式 AI 问答

| 模式 | 说明 |
|------|------|
| 本地模式 (`isLocal=true`) | 从 Chroma 向量库检索相关文档片段，基于本地知识库回答 |
| 联网模式 (`isLocal=false`) | 由 LLM 智能路由，按需调用 Tavily 联网搜索获取实时信息 |

两种模式均支持：多轮对话（SQLite 持久化）、Token 窗口管理（95000 tokens）、动态工具调用。

### 知识库管理

- 支持 **本地文件/目录**、**URL**、**GitHub 文件链接** 三种文档来源
- 支持 Markdown、PDF、YAML、TXT、HTML 格式
- 文档自动切分（1500 tokens / 200 overlap）、向量化后写入 Chroma
- 基于文件名 MD5 去重 + 内容 MD5 变更检测，自动增量更新
- 向量写入成功后才更新 SQLite 记录，失败可重试自愈

### 动态工具系统

- 工具描述存 SQLite，支持运行时通过 API/Web UI 增删改，自动刷新缓存
- 工具执行器通过 Spring Bean 自动注册，启动时与描述绑定
- 内置追踪日志（工具调用参数、返回结果、Token 消耗）

### 管理页面

| 页面 | 地址 |
|------|------|
| 文档管理 | http://localhost:6680/docs.html |
| 工具管理 | http://localhost:6680/tools.html |

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.4.5 |
| AI 框架 | LangChain4j | 1.12.2-beta22 |
| 对话模型 | DeepSeek Chat | OpenAI 兼容协议 |
| Embedding | 智谱 AI embedding-3 | — |
| 向量数据库 | Chroma | V2 API |
| 联网搜索 | Tavily | — |
| 数据存储 | SQLite | 3.49.1 |
| API 文档 | SpringDoc OpenAPI | 2.8.8 |
| 工具库 | Hutool 5.8、Fastjson2、Jsoup | — |

---

## 系统架构

```
┌───────────────────────────────────────────────────────────┐
│                  CLI 客户端 / 静态页面                      │
│           (go-ftc-console / docs.html / tools.html)        │
└──────────────────────────┬────────────────────────────────┘
                           │ HTTP REST / SSE
┌──────────────────────────▼────────────────────────────────┐
│                   Spring Boot Controllers                   │
│       AIChatController · AIEmbeddingController · AIToolController │
└──────────────────────────┬────────────────────────────────┘
                           │
      ┌────────────────────┼────────────────────┐
      ▼                    ▼                    ▼
AIChatService      AIEmbeddingService      AIToolService
      │                    │                    │
      ▼                    ▼                    ▼
┌───────────┐       ┌───────────┐       ┌───────────┐
│LocalAiSvc │       │ Ingestor  │       │ToolSpec   │
│WebAiSvc   │       │ + Chroma  │       │Repository │
└─────┬─────┘       └─────┬─────┘       └───────────┘
      │                    │
      ├──────────┬─────────┤
      ▼          ▼         ▼
 DeepSeek    Chroma     Tavily
 Chat API    向量库    Web Search
      │
      ├──── SQLite（会话记忆 + 工具描述 + 文档记录）
      └──── ToolRegistry（动态工具调用）
```

---

## 环境要求

| 服务 | 默认地址 | 用途 |
|------|----------|------|
| Chroma | http://localhost:8000 | 向量数据库 |
| DeepSeek API | https://api.deepseek.com | 对话模型 |
| 智谱 AI API | — | Embedding 向量化 |
| Tavily API | — | 联网搜索（Web 模式） |

```bash
# 启动 Chroma
docker run -d --name chroma -p 8000:8000 chromadb/chroma
```

---

## 快速开始

### 1. 配置环境变量

```bash
# 必需
set DEEPSEEK_API_KEY=sk-xxx
set ZHIPU_API_KEY=xxx
# 可选（联网搜索模式）
set TAVILY_API_KEY=tvly-xxx
# 可选（GitHub 文档加载）
set GITHUB_TOKEN=ghp_xxx
```

### 2. 启动服务

```bash
mvnw.cmd spring-boot:run
```

或打包后运行：

```bash
mvnw.cmd clean package -DskipTests
java -jar target/ftcli-0.0.1-SNAPSHOT.jar
```

### 3. 验证

- Swagger：http://localhost:6680/swagger-ui/index.html
- 文档管理：http://localhost:6680/docs.html
- 工具管理：http://localhost:6680/tools.html

---

## 配置说明

主配置：`src/main/resources/application.yaml`
敏感配置：`src/main/resources/application-local.yaml`（通过环境变量注入 API Key）


| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 6680 |
| `FTCLI_DB_PATH` | SQLite 数据库路径 | `${user.home}/.ftcli/ftcli.db` |
| `CHROMA_URL` | Chroma 向量库地址 | http://localhost:8000 |
| `DEEPSEEK_API_KEY` | DeepSeek 对话模型 Key | — |
| `ZHIPU_API_KEY` | 智谱 Embedding Key | — |
| `TAVILY_API_KEY` | Tavily 联网搜索 Key | — |
| `GITHUB_TOKEN` | GitHub 文档加载 Token | — |

SQLite 数据库在首次启动时由 `SqliteInitializer` 自动建表并初始化内置工具数据。

---

## API 接口

统一响应格式：

```json
{"code": 200, "message": "success", "data": {}}
```

### 聊天 `/api/rest/v1/ai`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/chatId` | 生成会话 ID |
| POST | `/chat` | 同步对话 |
| POST | `/chat/stream` | SSE 流式对话 |

请求体：

```json
{"isLocal": false, "chatId": "xxx", "userMessage": "你好"}
```

### 知识库 `/api/rest/v1/ai/embedding`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/docs` | 查询全部文档 |
| POST | `/docs` | 上传文档 |
| DELETE | `/docs/{id}` | 删除文档及向量 |

上传请求体：

```json
{"path": "C:/docs/my-project"}
```

> `path` 支持：本地文件/目录路径、URL 链接、GitHub 文件链接（自动识别）

### 工具管理 `/api/rest/v1/ai/tools`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 查询全部工具 |
| POST | `/` | 新增工具 |
| PUT | `/?oldName=xxx` | 更新工具 |
| DELETE | `/{name}` | 删除工具 |

---

## 内置工具

启动时通过 `sql/data.sql` 自动注册：

| 工具名 | 类型 | 描述 |
|--------|------|------|
| `getNowTime` | date | 获取当前时间 |
| `getRemainingTime` | date | 计算距当天结束的剩余时间 |
| `readFile` | file | 读取本地文件内容（支持行范围） |
| `writeFile` | file | 写入/追加本地文件 |
| `openFileOrDirectory` | system | 打开文件或目录 |
| `openEdgeWithUrl` | system | Edge 浏览器打开 URL |
| `openGitBashByPath` | system | 在指定目录打开 Git Bash |
| `getOSName` | system | 获取操作系统名称 |
| `runShell` | system | 执行 Shell 命令（支持超时） |

---

## 扩展工具

新增工具分两步：

**1. 实现执行器**（需重启）

```java
@Component
public class MyTool implements IToolExecutor {

    @Override
    public String getName() { return "myTool"; }

    @Override
    public boolean isMatch(ToolProviderRequest request) { return true; }

    @Override
    public ToolExecutor getToolExecutor() {
        return (req, memoryId) -> {
            Map<String, Object> args = JSON.parseObject(req.arguments());
            return "结果";
        };
    }
}
```

**2. 注册工具描述**（无需重启，通过 API 或 Web UI）

```bash
curl -X POST http://localhost:6680/api/rest/v1/ai/tools \
  -H "Content-Type: application/json" \
  -d '{"name":"myTool","description":"描述","type":"system","params":[{"name":"input","description":"参数","required":true,"type":"STRING"}]}'
```

参数类型：`STRING` / `INTEGER` / `NUMBER` / `BOOLEAN` / `ENUMS`

---

## 项目结构

```
ftcli/
├── pom.xml
└── src/main/
    ├── java/com/ftc/ftcli/
    │   ├── FtcliApplication.java
    │   ├── controller/              # REST 控制器
    │   │   ├── AIChatController
    │   │   ├── AIEmbeddingController
    │   │   └── AIToolController
    │   ├── service/                 # 业务层
    │   │   └── impl/
    │   ├── ai/
    │   │   ├── assistant/           # AI 服务接口（Local/Web）
    │   │   ├── store/               # SqliteChatMemoryStore
    │   │   └── tool/
    │   │       ├── ToolRegistry     # 工具注册中心
    │   │       ├── executor/        # 工具执行器
    │   │       │   └── impl/        # date/ file/ system/
    │   │       └── spec/            # 工具规格构建
    │   ├── config/
    │   │   ├── ai/                  # AI 助手、RAG、Embedding、追踪配置
    │   │   └── sqlite/              # SQLite 初始化
    │   ├── infra/sqlite/            # 数据访问层（ChatMemory/Embedding/ToolSpec）
    │   ├── entity/                  # 实体类
    │   ├── properties/              # 配置属性类
    │   └── common/
    │       ├── enums/               # 枚举
    │       └── util/                # 工具类（AI追踪/文档加载/GitHub解析）
    └── resources/
        ├── application.yaml
        ├── application-local.yaml   # API Key（环境变量注入）
        ├── prompt/                  # 系统提示词
        │   ├── local-service.markdown
        │   └── web-service.markdown
        ├── sql/                     # 建表 + 初始数据
        └── static/                  # 管理页面
            ├── docs.html
            └── tools.html
```

---

## 注意事项

1. **API Key**：所有密钥通过环境变量注入，`application-local.yaml` 中仅引用 `${ENV_VAR:}` 占位符
2. **SQLite 路径**：默认 `~/.ftcli/ftcli.db`，可通过 `FTCLI_DB_PATH` 环境变量覆盖
3. **外部依赖**：仅 Chroma 为必需外部服务（会话记忆和工具管理均使用本地 SQLite）
4. **工具热更新**：描述增删改通过 API 即时生效；执行器（Java 代码）变更需重启
5. **Chroma 版本**：使用 V2 API（`ChromaApiVersion.V2`），需 Chroma 0.4+

---

## 作者

冯铁城 — 17615007230@163.com
