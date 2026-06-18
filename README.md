# ftcli-ai-server

> FTC 个人 AI 命令行助手后端服务 —— 基于 LangChain4j 的大模型对话、RAG 知识库问答、Skill 技能系统与动态工具调用平台。

## 项目简介

**ftcli-ai-server** 是一个基于 Spring Boot 的 AI 助手服务，为 Go 编写的命令行客户端（[ftcli](https://github.com/GuardFTC/ftc-cli)）提供 AI 能力后端。项目以 [LangChain4j](https://github.com/langchain4j/langchain4j) 为核心，整合了：

- **大模型对话**：支持同步与 SSE 流式响应（DeepSeek Chat）
- **RAG 知识库**：支持本地文件、URL、GitHub 链接多来源文档的向量检索问答（含 Rerank 重排）
- **联网搜索**：通过 Tavily 获取实时信息，LLM 智能路由决策是否联网
- **Function Calling**：数据库驱动的动态工具调用框架，支持运行时热更新
- **Skill 技能系统**：支持动态加载/卸载技能，CRUD 操作后自动热重建 AI 服务

服务默认监听 `6680` 端口，同时提供 REST API 与静态管理页面。

---

## 核心特性

### 双模式 AI 问答

| 模式 | 说明 |
|------|------|
| 本地模式 (`isLocal=true`) | 从 Chroma 向量库检索相关文档片段 → Jina Rerank 重排 → 基于本地知识库回答 |
| 联网模式 (`isLocal=false`) | 由 LLM 智能路由（LanguageModelQueryRouter），按需调用 Tavily 联网搜索获取实时信息 |

两种模式均支持：多轮对话（SQLite 持久化）、Token 窗口管理（95000 tokens）、动态工具调用、Skill 技能激活。

### RAG 链路（本地模式）

```
用户提问
  → QueryTransformer（CompressingQueryTransformer，多轮对话查询压缩）
  → ContentRetriever（智谱 embedding-3 向量检索，top 5，min-score 0.5）
  → ContentAggregator（Jina reranker-v3 重排，保留 top 3）
  → ContentInjector（注入文档片段 + 元数据：文件名/路径）
  → LLM 生成回答
```

### RAG 链路（联网模式）

```
用户提问
  → QueryTransformer（CompressingQueryTransformer，多轮对话查询压缩）
  → QueryRouter（LanguageModelQueryRouter，LLM 判断是否需要联网）
  → ContentRetriever（Tavily Web Search，max 5 条）
  → LLM 生成回答
```

### 知识库管理

- 支持 **本地文件/目录**、**URL**、**GitHub 文件链接** 三种文档来源
- 支持 Markdown、PDF、YAML、TXT、HTML（含 Netscape 书签格式）格式
- 文档自动切分（1500 tokens / 200 overlap）、向量化后写入 Chroma
- 基于文件名 MD5 去重 + 内容 MD5 变更检测，自动增量更新
- 向量写入成功后才更新 SQLite 记录，失败可重试自愈
- 删除文档时同步清理 Chroma 中对应向量

### 动态工具系统

- 工具描述存 SQLite（tool_spec + tool_spec_param 两表），支持运行时通过 API/Web UI 增删改
- 工具执行器通过 Spring Bean 自动注册（`ToolExecutorFactory`），启动时与描述绑定（`ToolRegistry`）
- 工具 CRUD 操作后自动刷新内存缓存，无需重启
- 内置追踪日志（工具调用参数、返回结果、Token 消耗、检索命中、重排评分）
- 支持工具匹配策略（`IToolExecutor.isMatch`），可按用户消息动态决定是否提供某工具

### Skill 技能系统

- 技能存储在 SQLite skill 表中，支持通过 API 进行 CRUD
- 技能内容支持两种来源：直接存储 Markdown 内容 或 引用 resources 下的文件路径
- 技能 CRUD 后自动触发 `AiServiceHolder.buildAiService()` 热重建 AI 服务
- 加载后以 `Skills.toolProvider()` 形式注入 AI 服务，LLM 可通过 `activate_skill` 工具按需激活
- 系统提示词中自动拼接可用技能清单

### 管理页面

| 页面 | 地址 |
|------|------|
| 文档管理 | http://localhost:6680/docs.html |
| 工具管理 | http://localhost:6680/tools.html |
| 技能管理 | http://localhost:6680/skills.html |

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
| Rerank | Jina reranker-v3 | — |
| 数据存储 | SQLite | 3.49.1 |
| API 文档 | SpringDoc OpenAPI | 2.8.8 |
| 工具库 | Hutool 5.8.28、Fastjson2 2.0.61、Jsoup 1.21.2 | — |

---

## 系统架构

```
┌───────────────────────────────────────────────────────────┐
│                  CLI 客户端 / 静态页面                      │
│     (go-ftc-console / docs.html / tools.html / skills.html)│
└──────────────────────────┬────────────────────────────────┘
                           │ HTTP REST / SSE
┌──────────────────────────▼────────────────────────────────┐
│                   Spring Boot Controllers                   │
│  AIChatController · AIEmbeddingController                  │
│  AIToolController · AISkillController                      │
└──────────────────────────┬────────────────────────────────┘
                           │
      ┌────────────────────┼────────────────────┐
      ▼                    ▼                    ▼
AIChatService      AIEmbeddingService    AIToolService / AISkillService
      │                    │                    │
      ▼                    ▼                    ▼
┌─────────────┐     ┌───────────┐       ┌───────────────┐
│AiServiceHolder│    │ Ingestor  │       │ToolSpec/Skill │
│ LocalAiSvc  │     │ + Chroma  │       │  Repository   │
│ WebAiSvc    │     └─────┬─────┘       └───────────────┘
└──────┬──────┘           │
       │                  │
       ├──────────┬───────┤
       ▼          ▼       ▼
  DeepSeek    Chroma   Tavily        Jina
  Chat API    向量库  Web Search   Reranker
       │
       ├──── SQLite（会话记忆 + 工具描述 + 文档记录 + 技能）
       ├──── ToolRegistry（动态工具调用）
       └──── Skills（动态技能激活）
```

---

## 环境要求

| 服务 | 默认地址 | 用途 |
|------|----------|------|
| Chroma | http://localhost:8000 | 向量数据库 |
| DeepSeek API | https://api.deepseek.com | 对话模型 |
| 智谱 AI API | — | Embedding 向量化 |
| Tavily API | — | 联网搜索（Web 模式） |
| Jina API | — | Rerank 重排（本地模式） |

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
set JINA_API_KEY=jina_xxx
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
- 技能管理：http://localhost:6680/skills.html

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
| `JINA_API_KEY` | Jina Rerank Key | — |
| `TAVILY_API_KEY` | Tavily 联网搜索 Key | — |
| `GITHUB_TOKEN` | GitHub 文档加载 Token | — |

### AI 相关配置

```yaml
ai:
  chat-memory:
    max-tokens: 95000              # Token 窗口大小
    token-estimator-model: gpt-4o  # Token 计数估算模型
  embedding:
    model:
      zhipu:
        model-name: embedding-3    # 智谱 Embedding 模型
    store:
      chroma:
        url: ${CHROMA_URL:http://localhost:8000}
        tenant: langchain4j
        database: langchain4j_db
        collection: langchain4j_coll
  rag:
    ingestor:
      max-segment-size: 1500       # 文档切分最大段落（token）
      overlap: 200                 # 切分重叠（token）
      token-estimator-model: gpt-4o
    web-search:
      max-results: 5               # Tavily 最大搜索结果数
    content-retriever:
      max-results: 5               # 向量检索最大结果数
      min-score: 0.5               # 向量检索最小相似度
    rerank:
      model: jina-reranker-v3      # Rerank 模型
      max-results: 3               # Rerank 后保留结果数
```

SQLite 数据库在首次启动时由 `SqliteInitializer` 自动建表并初始化内置工具数据。

---

## API 接口

统一响应格式：

```json
{"code": 200, "message": "响应成功", "data": {}}
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

### 技能管理 `/api/rest/v1/ai/skills`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 查询全部技能 |
| GET | `/{id}` | 根据 ID 查询技能 |
| POST | `/` | 新增技能 |
| PUT | `/?oldName=xxx` | 更新技能 |
| DELETE | `/{id}` | 删除技能 |

技能请求体：

```json
{
  "skillName": "git-commit",
  "skillDescription": "分析 Git 变更并生成 Conventional Commits 格式的提交信息",
  "skillMdContent": "## 目标\n...",
  "skillMdPath": ""
}
```

> `skillMdContent` 和 `skillMdPath` 二选一，前者直接存储内容，后者引用 resources 下的文件路径。

---

## 内置工具

启动时通过 `sql/data.sql` 自动注册：

| 工具名 | 类型 | 描述 |
|--------|------|------|
| `getNowTime` | date | 获取当前时间（yyyy-MM-dd HH:mm:ss） |
| `getRemainingTime` | date | 计算距当天结束的剩余时间（支持 HOUR/MINUTE/SECOND） |
| `readFile` | file | 读取本地文件内容（支持行范围） |
| `writeFile` | file | 写入/追加本地文件（自动创建目录） |
| `openFileOrDirectory` | system | 打开文件或目录（跨平台） |
| `openEdgeWithUrl` | system | Edge 浏览器打开 URL |
| `openGitBashByPath` | system | 在指定目录打开 Git Bash |
| `getOSName` | system | 获取操作系统名称（小写） |
| `runShell` | system | 执行 Shell 命令（支持超时、跨平台） |

---

## 扩展工具

新增工具分两步：

**1. 实现执行器**（需重启）

```java
@Component
public class MyTool implements IToolExecutor {

    @Override
    public String getName() {
        return Introspector.decapitalize(this.getClass().getSimpleName());
    }

    @Override
    public boolean isMatch(ToolProviderRequest request) {
        return true;
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (req, memoryId) -> {
            Map<String, Object> args = JSON.parseObject(req.arguments());
            // 业务逻辑
            return "结果";
        };
    }
}
```

**2. 注册工具描述**（无需重启，通过 API 或 Web UI）

```bash
curl -X POST http://localhost:6680/api/rest/v1/ai/tools ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"myTool\",\"description\":\"描述\",\"type\":\"system\",\"params\":[{\"name\":\"input\",\"description\":\"参数\",\"required\":true,\"type\":\"STRING\"}]}"
```

参数类型：`STRING` / `INTEGER` / `NUMBER` / `BOOLEAN` / `ENUMS`

---

## 扩展技能

新增技能无需重启，通过 API 即可：

```bash
curl -X POST http://localhost:6680/api/rest/v1/ai/skills ^
  -H "Content-Type: application/json" ^
  -d "{\"skillName\":\"my-skill\",\"skillDescription\":\"技能描述\",\"skillMdContent\":\"# 技能内容\\n...\",\"skillMdPath\":\"\"}"
```

或通过 `skillMdPath` 引用 resources 下的文件：

```json
{
  "skillName": "git-commit",
  "skillDescription": "生成 Conventional Commits 格式的提交信息",
  "skillMdContent": "",
  "skillMdPath": "skills/example/SKILL.md"
}
```

技能加载后，LLM 在系统提示词中会看到可用技能清单，并通过 `activate_skill` 工具按需激活。

---

## 项目结构

```
ftcli-ai-server/
├── pom.xml
└── src/main/
    ├── java/com/ftc/ftcli/
    │   ├── FtcliApplication.java          # 启动类
    │   │
    │   ├── controller/                    # REST 控制器
    │   │   ├── AIChatController           # 聊天（同步/流式）
    │   │   ├── AIEmbeddingController      # 知识库文档管理
    │   │   ├── AIToolController           # 工具 CRUD
    │   │   └── AISkillController          # 技能 CRUD
    │   │
    │   ├── service/                       # 业务层接口
    │   │   ├── AiChatService
    │   │   ├── AIEmbeddingService
    │   │   ├── AIToolService
    │   │   ├── AISkillService
    │   │   └── impl/                      # 业务层实现
    │   │       ├── AIChatServiceImpl
    │   │       ├── AIEmbeddingServiceImpl
    │   │       ├── AIToolServiceImpl
    │   │       └── AISkillServiceImpl
    │   │
    │   ├── ai/
    │   │   ├── service/                   # AI 服务层
    │   │   │   ├── AiServiceHolder        # AI 服务持有者（支持热重建）
    │   │   │   ├── LocalAiService         # 本地 RAG 问答接口
    │   │   │   └── WebAiService           # 联网搜索问答接口
    │   │   ├── store/
    │   │   │   └── SqliteChatMemoryStore  # 聊天记忆 SQLite 持久化
    │   │   └── tool/
    │   │       ├── ToolRegistry           # 工具注册中心（启动加载+动态刷新）
    │   │       ├── ToolTypeEnum           # 工具类型枚举
    │   │       ├── executor/
    │   │       │   ├── IToolExecutor      # 工具执行器接口
    │   │       │   ├── ToolExecutorFactory # 执行器工厂（Spring Bean 自动发现）
    │   │       │   └── impl/
    │   │       │       ├── date/
    │   │       │       │   ├── GetNowTime
    │   │       │       │   └── GetRemainingTime
    │   │       │       ├── file/
    │   │       │       │   ├── ReadFile
    │   │       │       │   └── WriteFile
    │   │       │       └── system/
    │   │       │           ├── GetOSName
    │   │       │           ├── OpenEdgeWithUrl
    │   │       │           ├── OpenFileOrDirectory
    │   │       │           ├── OpenGitBashByPath
    │   │       │           └── RunShell
    │   │       └── spec/
    │   │           ├── ToolSpecEntity     # 工具规格实体
    │   │           ├── ToolSpecParamEntity # 工具参数实体
    │   │           ├── ToolParamTypeEnum  # 参数类型枚举
    │   │           └── ToolSpecBuilder    # 工具规格构建器
    │   │
    │   ├── config/
    │   │   ├── ai/
    │   │   │   ├── RagConfig              # RAG 全链路配置（检索/路由/重排/注入）
    │   │   │   ├── EmbeddingConfig        # Embedding 模型 + Chroma + GitHub Loader
    │   │   │   └── AIRequestLogConfig     # LLM 请求响应追踪监听器
    │   │   └── sqlite/
    │   │       ├── SqliteConfiguration    # 数据源配置（确保目录存在）
    │   │       └── SqliteInitializer      # 启动时自动建表+初始化数据
    │   │
    │   ├── infra/sqlite/                  # 数据访问层（JdbcTemplate）
    │   │   ├── ChatMemoryRepository       # 聊天记忆 CRUD
    │   │   ├── EmbeddingRecordRepository  # 文档记录 CRUD
    │   │   ├── ToolSpecRepository         # 工具规格 CRUD
    │   │   └── SkillRepository            # 技能 CRUD
    │   │
    │   ├── entity/                        # 实体类
    │   │   ├── chat/
    │   │   │   └── ChatMemoryEntity
    │   │   ├── embedding/
    │   │   │   ├── EmbeddingFileUploadPayload
    │   │   │   ├── EmbeddingFileUploadResult
    │   │   │   └── EmbeddingRecordEntity
    │   │   ├── payload/
    │   │   │   └── ChatPayload
    │   │   ├── result/
    │   │   │   └── RestfulResult
    │   │   └── skill/
    │   │       └── SkillEntity
    │   │
    │   ├── properties/                    # 配置属性类
    │   │   ├── chat/
    │   │   │   └── ChatMemoryProperties
    │   │   ├── embedding/
    │   │   │   ├── EmbeddingModelProperties
    │   │   │   ├── EmbeddingStoreProperties
    │   │   │   └── EmbeddingGithubProperties
    │   │   └── rag/
    │   │       ├── ContentRetrieverProperties
    │   │       ├── RagIngestorProperties
    │   │       ├── RerankProperties
    │   │       └── WebSearchProperties
    │   │
    │   └── common/
    │       ├── enums/
    │       │   ├── doc/
    │       │   │   ├── DocLoaderEnum       # 文档加载器类型
    │       │   │   ├── DocMetaDataKeyEnum  # 文档元数据 Key
    │       │   │   └── DocParserTypeEnum   # 文档解析器类型
    │       │   └── result/
    │       │       └── RestfulResultEnum
    │       └── util/
    │           ├── ai/
    │           │   └── AiTraceLog          # AI 链路追踪日志工具
    │           ├── doc/
    │           │   ├── DocUtil             # 文档工具类
    │           │   ├── doc_loader/
    │           │   │   ├── IDocLoader      # 文档加载器接口
    │           │   │   ├── DocLoaderFactory # 加载器工厂
    │           │   │   └── impl/
    │           │   │       ├── FileSystemDocLoader
    │           │   │       ├── UrlDocLoader
    │           │   │       └── GithubDocLoader
    │           │   └── doc_parser/
    │           │       ├── DocParserFactory # 解析器工厂
    │           │       └── impl/
    │           │           └── HtmlDocumentParser  # HTML/书签解析
    │           └── github/
    │               ├── GitHubUrlParser     # GitHub URL 解析
    │               └── GitHubUrlInfo       # GitHub URL 信息
    │
    └── resources/
        ├── application.yaml               # 主配置
        ├── application-local.yaml         # API Key（环境变量注入）
        ├── prompt/
        │   ├── local-service.md           # 本地模式系统提示词
        │   └── web-service.md             # 联网模式系统提示词
        ├── sql/
        │   ├── schema.sql                 # 建表 DDL
        │   └── data.sql                   # 初始数据（内置工具）
        ├── skills/
        │   └── example/
        │       └── SKILL.md               # 示例技能（Git Commit）
        └── static/                        # 管理页面
            ├── docs.html
            ├── tools.html
            └── skills.html
```

---

## 数据库表结构

### tool_spec（工具规格表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| name | TEXT UNIQUE | 工具名称 |
| description | TEXT | 工具描述 |
| type | TEXT | 工具类型（date/file/system） |

### tool_spec_param（工具参数表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| tool_spec_id | INTEGER FK | 关联 tool_spec.id |
| name | TEXT | 参数名称 |
| description | TEXT | 参数描述 |
| required | INTEGER | 是否必填（0/1） |
| type | TEXT | 参数类型（string/integer/number/boolean/enums） |
| enum_values | TEXT | 枚举值（逗号分隔） |

### embedding_record（文档记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| file_name | TEXT | 文件名 |
| file_path | TEXT | 文件源路径 |
| file_name_md5 | TEXT UNIQUE | 文件名 MD5（去重） |
| file_content_md5 | TEXT | 文件内容 MD5（变更检测） |
| created_at | TEXT | 首次录入时间 |
| updated_at | TEXT | 最近更新时间 |

### chat_memory（聊天记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| memory_id | TEXT UNIQUE | 会话 ID |
| chat_message | TEXT | 聊天消息（JSON 序列化） |

### skill（技能表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| skill_name | TEXT UNIQUE | 技能名称 |
| skill_description | TEXT | 技能描述 |
| skill_md_content | TEXT | SKILL.md 文件内容 |
| skill_md_path | TEXT | SKILL.md 文件路径（resources 相对路径） |
| created_at | TEXT | 创建时间 |
| updated_at | TEXT | 更新时间 |

---

## 链路追踪日志

项目内置完整的 AI 链路追踪（`AiTraceLog`），运行时可在控制台看到：

```
[AI-Trace] 查询压缩: [原始query] -> [压缩后query]
[AI-Trace] 检索查询: [query]
[AI-Trace] 检索命中: [N]条
[AI-Trace]  -来源=[file_name], 内容=[preview...]
[AI-Trace] 重排序: query=[...], 检索文档数=[5], 结果文档数=[3]
[AI-Trace]  -[1] score=[0.8234], 来源=[file], 内容=[...]
[AI-Trace] 工具调用: name=[getNowTime], args=[{}]
[AI-Trace] 工具返回: name=[getNowTime], result=[2026-06-16 17:30:00]
[AI-Trace] Token使用: input=[1234], output=[567], total=[1801]
[AI-Trace] 总耗时: [3秒]
```

---

## 注意事项

1. **API Key**：所有密钥通过环境变量注入，`application-local.yaml` 中仅引用 `${ENV_VAR:}` 占位符
2. **SQLite 路径**：默认 `~/.ftcli/ftcli.db`，可通过 `FTCLI_DB_PATH` 环境变量覆盖
3. **外部依赖**：仅 Chroma 为必需外部服务（会话记忆和工具管理均使用本地 SQLite）
4. **工具热更新**：描述增删改通过 API 即时生效；执行器（Java 代码）变更需重启
5. **技能热更新**：技能 CRUD 通过 API 即时生效，自动触发 AI 服务重建
6. **Chroma 版本**：使用 V2 API（`ChromaApiVersion.V2`），需 Chroma 0.4+
7. **漏斗策略**：向量检索 5 条 → Rerank 保留 3 条，适合小规模个人知识库
8. **系统提示词**：位于 `resources/prompt/` 目录，AI 人格为温柔体贴的女友风格
9. **输出格式**：AI 输出针对 CLI 终端优化，禁用 Markdown 语法，使用【】、「」等替代格式

---

## 作者

冯铁城 — 17615007230@163.com
