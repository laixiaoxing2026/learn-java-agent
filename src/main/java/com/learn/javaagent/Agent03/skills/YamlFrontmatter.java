package com.learn.javaagent.Agent03.skills;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解析以 {@code ---} 分隔的 YAML frontmatter（仅支持简单 {@code key: value} 单行键值，与常见 SKILL.md 约定一致）。
 *
 * @author 298751
 */
final class YamlFrontmatter {

    private YamlFrontmatter() {
    }

    /**
     * frontmatter 与正文拆分结果。
     */
    static final class SplitResult {
        private final Map<String, String> meta;
        private final String body;

        SplitResult(Map<String, String> meta, String body) {
            this.meta = meta;
            this.body = body;
        }

        Map<String, String> getMeta() {
            return meta;
        }

        String getBody() {
            return body;
        }
    }

    /**
     * 将全文拆为 meta 与 body；若无合法 frontmatter，则 meta 为空、全文为 body。
     *
     * @param text 文件全文
     * @return meta + body
     */
    static SplitResult splitMetaAndBody(String text) {
        if (text == null || !text.startsWith("---")) {
            return new SplitResult(new LinkedHashMap<>(), text == null ? "" : text);
        }
        int line2 = text.indexOf('\n', 3);
        if (line2 < 0) {
            return new SplitResult(new LinkedHashMap<>(), text);
        }
        int endFm = text.indexOf("\n---", line2);
        if (endFm < 0) {
            return new SplitResult(new LinkedHashMap<>(), text);
        }
        String fmBlock = text.substring(line2 + 1, endFm).trim();
        String body = text.length() > endFm + 4 ? text.substring(endFm + 4).replaceFirst("^\r?\n", "") : "";
        return new SplitResult(parseSimpleYaml(fmBlock), body);
    }

    /**
     * 解析仅含 {@code key: value} 的行（冒号后可为空；忽略空行与 {@code #} 注释行）。
     */
    private static Map<String, String> parseSimpleYaml(String block) {
        Map<String, String> m = new LinkedHashMap<>();
        if (block == null || block.isEmpty()) {
            return m;
        }
        for (String rawLine : block.split("\r?\n")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            // 去掉可能的引号
            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }
            m.put(key, value);
        }
        return m;
    }
}
