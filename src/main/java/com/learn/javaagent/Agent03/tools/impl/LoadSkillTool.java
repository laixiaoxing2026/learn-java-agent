package com.learn.javaagent.Agent03.tools.impl;

import com.google.gson.JsonObject;
import com.learn.javaagent.Agent03.skills.SkillLoader;
import com.learn.javaagent.Agent03.tools.Tool;

import java.util.Objects;

/**
 * 按需加载技能全文：将完整说明通过 tool 消息正文返回（不写入 system prompt），供模型在需要时拉取。
 *
 * @author 298751
 */
public final class LoadSkillTool implements Tool {

    private final SkillLoader loader;

    /**
     * @param loader 技能加载器（通常使用 {@link SkillLoader#getDefault()}）
     */
    public LoadSkillTool(SkillLoader loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    @Override
    public String name() {
        return "load_skill";
    }

    @Override
    public String description() {
        return "Load full instructions for a named skill. The skill text is returned in this tool result "
                + "(XML wrapper), not in the system prompt. Call when you need detailed guidance for a skill "
                + "listed under \"Skills available\".";
    }

    @Override
    public JsonObject parametersSchema() {
        JsonObject props = new JsonObject();
        props.add("name", Tool.stringProperty("Skill id (same as listed under Skills available, e.g. git, test)"));
        return objectSchema(props, "name");
    }

    @Override
    public String execute(String argumentsJson) {
        JsonObject o = parseObject(argumentsJson);
        String skillName = str(o, "name");
        return loader.getContent(skillName);
    }
}
