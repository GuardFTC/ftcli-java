-- 工具规格表
CREATE TABLE IF NOT EXISTS tool_spec
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    type        TEXT NOT NULL
);

-- 工具参数表
CREATE TABLE IF NOT EXISTS tool_spec_param
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    tool_spec_id  INTEGER NOT NULL,
    name          TEXT    NOT NULL,
    description   TEXT    NOT NULL,
    required      INTEGER NOT NULL DEFAULT 0,
    type          TEXT    NOT NULL,
    enum_values   TEXT,
    FOREIGN KEY (tool_spec_id) REFERENCES tool_spec (id),
    UNIQUE (tool_spec_id, name)
);

-- Embedding文档记录表
CREATE TABLE IF NOT EXISTS embedding_record
(
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name        TEXT NOT NULL,
    file_path        TEXT NOT NULL,
    file_name_md5    TEXT NOT NULL,
    file_content_md5 TEXT NOT NULL,
    created_at       TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at       TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 文件名MD5唯一索引，防止同一文件重复录入
CREATE UNIQUE INDEX IF NOT EXISTS idx_embedding_record_name_md5 ON embedding_record(file_name_md5);
