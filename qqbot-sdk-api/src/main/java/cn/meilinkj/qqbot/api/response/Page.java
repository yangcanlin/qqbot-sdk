package cn.meilinkj.qqbot.api.response;

import java.util.List;

/**
 * cursor 分页结果包装：数据列表 + 下一页 cursor（为空表示无下一页）。
 */
public class Page<T> {

    private final List<T> items;
    private final String nextCursor;

    public Page(List<T> items, String nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<T> getItems() {
        return items;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public boolean hasNext() {
        return nextCursor != null && !nextCursor.isEmpty();
    }
}
