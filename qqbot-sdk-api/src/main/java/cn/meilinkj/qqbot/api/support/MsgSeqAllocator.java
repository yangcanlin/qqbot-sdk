package cn.meilinkj.qqbot.api.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 被动回复 msg_seq 分配器：同一 msg_id 多次回复时序号自动递增，避免平台去重导致发送失败。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息-msg_seq</a>
 */
public class MsgSeqAllocator {

    private final Map<String, AtomicInteger> seqMap = new ConcurrentHashMap<>();

    /** 返回该 msg_id 的下一个可用序号（首次为 1）。 */
    public int next(String msgId) {
        return seqMap.computeIfAbsent(msgId, k -> new AtomicInteger()).incrementAndGet();
    }
}