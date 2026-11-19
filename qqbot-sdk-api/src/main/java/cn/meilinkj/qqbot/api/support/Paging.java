package cn.meilinkj.qqbot.api.support;

import cn.meilinkj.qqbot.api.response.Page;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** cursor 分页查询串与响应解析 helper，供各 API 类共用。 */
public final class Paging {

    private Paging() {
    }

    public static StringBuilder query() {
        return new StringBuilder();
    }

    public static StringBuilder cursor(String cursor) {
        StringBuilder query = query();
        add(query, "cursor", cursor);
        return query;
    }

    public static void add(StringBuilder query, String name, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        query.append(query.length() == 0 ? '?' : '&').append(name).append('=').append(encode(value));
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 官方 cursor 分页响应：数组字段名因接口而异（members / users / list / records 等），next_cursor 为空串表示已到最后一页。 */
    public static <T> Page<T> page(JsonNode node, String arrayField, Function<JsonNode, List<T>> itemsMapper) {
        if (node == null) {
            return new Page<>(Collections.emptyList(), null);
        }
        JsonNode items = node.get(arrayField);
        List<T> list = items == null ? Collections.emptyList() : itemsMapper.apply(items);
        return new Page<>(list, node.path("next_cursor").asText(null));
    }
}
