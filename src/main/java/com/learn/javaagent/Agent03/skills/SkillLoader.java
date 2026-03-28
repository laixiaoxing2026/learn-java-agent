package com.learn.javaagent.Agent03.skills;

import com.learn.javaagent.Agent03.config.AgentConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 递归扫描技能根目录下所有 {@code SKILL.md}，解析 YAML frontmatter；技能标识为 frontmatter 中的 {@code name}，
 * 缺省为技能目录名（含 SKILL.md 的父目录名）。
 * <p>
 * 第一层（系统提示）：{@link #getDescriptions()} 仅列出名称与简短描述；第二层（按需）：{@link #getContent(String)}
 * 返回 {@code &lt;skill name="..."&gt;...&lt;/skill&gt;}，供 {@code load_skill} 工具写入 tool 消息。
 * </p>
 *
 * @author 298751
 */
public final class SkillLoader {

    private static volatile SkillLoader defaultInstance;

    private final Map<String, SkillEntry> skills;

    /**
     * @param skillsDir 技能根目录（其下各子目录含 {@code SKILL.md}）
     */
    public SkillLoader(Path skillsDir) throws IOException {
        this.skills = Collections.unmodifiableMap(loadAll(Objects.requireNonNull(skillsDir, "skillsDir")));
    }

    /**
     * 空加载器（无技能、不读盘）。
     */
    private SkillLoader(Map<String, SkillEntry> prebuilt) {
        this.skills = Collections.unmodifiableMap(new LinkedHashMap<>(prebuilt));
    }

    /**
     * 进程内单例：根目录由 {@link AgentConfig#skillsDirectory()} 解析（含 {@code agent.properties} / 环境变量）。
     */
    public static SkillLoader getDefault() {
        SkillLoader ref = defaultInstance;
        if (ref != null) {
            return ref;
        }
        synchronized (SkillLoader.class) {
            if (defaultInstance == null) {
                try {
                    defaultInstance = new SkillLoader(AgentConfig.skillsDirectory());
                } catch (IOException e) {
                    defaultInstance = empty();
                }
            }
            return defaultInstance;
        }
    }

    /**
     * @return 无技能、无 IO 的加载器
     */
    public static SkillLoader empty() {
        return new SkillLoader(Collections.<String, SkillEntry>emptyMap());
    }

    /**
     * @return 供系统提示使用的多行列表（与 Python {@code get_descriptions} 对齐）
     */
    public String getDescriptions() {
        if (skills.isEmpty()) {
            return "";
        }
        List<String> names = new ArrayList<>(skills.keySet());
        Collections.sort(names);
        StringBuilder lines = new StringBuilder();
        for (String name : names) {
            SkillEntry e = skills.get(name);
            String desc = e.getMeta().getOrDefault("description", "");
            lines.append("  - ").append(name).append(": ").append(desc).append("\n");
        }
        if (lines.length() > 0 && lines.charAt(lines.length() - 1) == '\n') {
            lines.setLength(lines.length() - 1);
        }
        return lines.toString();
    }

    /**
     * @param name 技能名（与 frontmatter {@code name} 或目录名一致）
     * @return 包装后的 XML 片段，供 tool_result 注入；未知技能返回错误句
     */
    public String getContent(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Error: skill name is empty.";
        }
        String key = name.trim();
        SkillEntry skill = skills.get(key);
        if (skill == null) {
            return "Error: Unknown skill '" + key + "'.";
        }
        String body = skill.getBody();
        return "<skill name=\"" + escapeAttr(key) + "\">\n" + body + "\n</skill>";
    }

    /**
     * @return 是否至少加载了一个技能
     */
    public boolean isEmpty() {
        return skills.isEmpty();
    }

    private static Map<String, SkillEntry> loadAll(Path skillsDir) throws IOException {
        Map<String, SkillEntry> map = new LinkedHashMap<>();
        if (!Files.isDirectory(skillsDir)) {
            return map;
        }
        List<Path> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(skillsDir)) {
            walk.filter(p -> p.getFileName() != null && "SKILL.md".equalsIgnoreCase(p.getFileName().toString()))
                    .sorted()
                    .forEach(files::add);
        }
        for (Path f : files) {
            String text = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
            YamlFrontmatter.SplitResult split = YamlFrontmatter.splitMetaAndBody(text);
            Map<String, String> meta = split.getMeta();
            String body = split.getBody();
            Path parent = f.getParent();
            String dirName = parent != null && parent.getFileName() != null
                    ? parent.getFileName().toString()
                    : "";
            String skillName = meta.containsKey("name") && !meta.get("name").trim().isEmpty()
                    ? meta.get("name").trim()
                    : dirName;
            if (skillName.isEmpty()) {
                continue;
            }
            map.put(skillName, new SkillEntry(meta, body));
        }
        return map;
    }

    /** XML 属性值转义 */
    private static String escapeAttr(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
