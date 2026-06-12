-- 工具数据
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('getNowTime', '获取当前时间，返回格式为yyyy-MM-dd HH:mm:ss', 'date');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('getRemainingTime', '计算当前时间距离当天结束还有多少时间', 'date');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('readFile',
        '读取本地文件的内容。强烈注意：当需要查看、读取或检查任何文件内容时，必须优先使用此工具！严禁优先使用 Shell、终端或命令行命令（如 cat, type, Get-Content）来读取文件。',
        'file');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('writeFile',
        '将内容写入本地文件。强烈注意：当写入任何文件到本地文件时，必须优先使用此工具！严禁优先使用 Shell、终端或命令行命令来写入文件。',
        'file');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('openFileOrDirectory', '根据输入的文件/文件夹路径 打开对应的文件/文件夹', 'system');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('openEdgeWithUrl', '通过输入的网站URL，基于Edge浏览器访问该网页', 'system');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('openGitBashByPath', '在指定的目录路径下打开 Git Bash 终端', 'system');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('getOSName', '获取操作系统名称(小写)', 'system');
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('runShell', '执行Shell命令', 'system');

-- 工具参数数据
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'openFileOrDirectory'), 'path', '文件/文件夹路径', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'openEdgeWithUrl'), 'url', '网站URL', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'openGitBashByPath'), 'path', '指定的目录路径', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'runShell'), 'command', 'Shell命令', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'runShell'), 'filePath', 'Shell命令执行的文件夹，缺省时默认值为: 当前登录用户的主目录', 0, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'runShell'), 'timeout', 'Shell命令执行的超时时间/秒，缺省时默认值为:10', 0, 'integer', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'readFile'), 'endLine', '结束读取行数 缺省时读取全文', 0, 'integer', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'readFile'), 'filePath', '文件路径', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'readFile'), 'startLine', '开始读取行数 缺省时读取全文', 0, 'integer', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'writeFile'), 'filePath', '写入文件路径', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'writeFile'), 'content', '写入文件内容，多行用系统换行符分割', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'writeFile'), 'isAppend', '是否为追加模式 ture-追加内容 false-替换内容', 1, 'boolean', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'getRemainingTime'), 'nowDateStr', '当前时间,格式为yyyy-MM-dd HH:mm:ss', 1, 'string', null);
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'getRemainingTime'), 'unit', '时间单位', 0, 'enums', 'HOUR,MINUTE,SECOND');