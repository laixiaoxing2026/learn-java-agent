package com.learn.javaagent.Agent03.skills;

import java.util.Collections;
import java.util.Map;

/**
 * 单个技能条目：YAML frontmatter 元数据 + 正文（不含 frontmatter）。
 *
 * @author 298751
 */
public final class SkillEntry {

    private final Map<String, String> meta;
    private final String body;

    /**
     * @param meta frontmatter 键值（不可变视图）
     * @param body Markdown 正文
     */
    public SkillEntry(Map<String, String> meta, String body) {
        this.meta = meta == null ? Collections.emptyMap() : Collections.unmodifiableMap(meta);
        this.body = body == null ? "" : body;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public String getBody() {
        return body;
    }
}
