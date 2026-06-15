---
name: git-commit
description: 分析 Git 工作区变更并生成规范的 Commit Message，然后执行提交
---

## 目标
分析用户当前 Git 仓库的未提交变更，生成符合 Conventional Commits 规范的 commit message，并完成提交。

## 执行步骤

### 1. 获取变更信息
依次调用以下命令获取当前仓库状态：

```
git status --short
```

如果没有任何变更，直接告知用户"当前工作区没有待提交的变更"，结束流程。

### 2. 获取变更详情
对有变更的文件获取 diff 内容：

```
git diff
git diff --cached
```

### 3. 分析变更并生成 Commit Message

根据 diff 内容，按以下规范生成 commit message：

**格式：**
```
<type>(<scope>): <subject>

<body>
```

**type 取值规则：**
- `feat`: 新功能
- `fix`: 修复 Bug
- `refactor`: 重构（既不是新功能也不是修复）
- `docs`: 文档变更
- `style`: 代码格式调整（不影响逻辑）
- `test`: 测试相关
- `chore`: 构建/工具/依赖变更
- `perf`: 性能优化

**scope 规则：**
- 根据变更文件的模块/包名推断，例如 `controller`、`service`、`config`
- 如果涉及多个模块，可省略 scope

**subject 规则：**
- 使用中文描述（跟随用户语言习惯）
- 不超过 50 个字符
- 不加句号结尾
- 使用祈使语气

**body 规则：**
- 如果变更较复杂（涉及3个以上文件或逻辑变更较大），用简洁的列表说明主要改动点
- 简单变更可省略 body

### 4. 确认并执行提交

将生成的 commit message 展示给用户确认。用户确认后执行：

```
git add -A
git commit -m "<生成的commit message>"
```

**注意事项：**
- commit message 中如果包含双引号，需要转义
- 如果用户要求只提交部分文件，使用 `git add <file>` 替代 `git add -A`
- 提交完成后，展示 `git log --oneline -1` 的结果作为确认

## 示例

### 输入
用户说："帮我提交一下"

### 执行过程
1. 执行 `git status --short`，发现：
   ```
   M  src/main/java/com/ftc/ftcli/config/ai/AiAssistantConfig.java
   M  src/main/java/com/ftc/ftcli/service/impl/AIChatServiceImpl.java
   ```
2. 执行 `git diff --cached` 和 `git diff` 查看具体改动
3. 分析得出：修复了 StreamingChatModel 未注入的问题
4. 生成 commit message：
   ```
   fix(config): 修复 StreamingChatModel 未通过构造器注入的问题

   - AiAssistantConfig 中 streamingModel 字段添加 final 修饰符
   - 确保 @RequiredArgsConstructor 能正确注入 StreamingChatModel Bean
   ```
5. 展示给用户确认
6. 执行 `git add -A` + `git commit -m "..."`
7. 展示最终提交记录
