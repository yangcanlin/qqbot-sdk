package cn.meilinkj.qqbot.core.gateway;

/**
 * 事件订阅 Intents 位掩码，严格对齐官方文档：
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html#事件订阅intents">事件订阅 Intents</a>
 *
 * <p>除 GUILDS、PUBLIC_GUILD_MESSAGES、GUILD_MEMBERS 为基础事件外，
 * 其余特殊事件需向平台申请权限后才能订阅。</p>
 */
public enum Intents {

    /** 频道事件：GUILD_CREATE/UPDATE/DELETE、CHANNEL_CREATE/UPDATE/DELETE (1&lt;&lt;0) */
    GUILDS(1L << 0),
    /** 频道成员事件：GUILD_MEMBER_ADD/UPDATE/REMOVE (1&lt;&lt;1) */
    GUILD_MEMBERS(1L << 1),
    /** 私域频道消息事件：MESSAGE_CREATE、MESSAGE_DELETE (1&lt;&lt;9) */
    GUILD_MESSAGES(1L << 9),
    /** 频道表情表态事件：MESSAGE_REACTION_ADD/REMOVE (1&lt;&lt;10) */
    GUILD_MESSAGE_REACTIONS(1L << 10),
    /** 频道私信事件：DIRECT_MESSAGE_CREATE、DIRECT_MESSAGE_DELETE (1&lt;&lt;12) */
    DIRECT_MESSAGE(1L << 12),
    /** 群成员事件：GROUP_MEMBER_ADD/REMOVE、GROUP_JOIN_REQUEST (1&lt;&lt;24) */
    GROUP_MEMBER_EVENT(1L << 24),
    /** QQ 群聊与单聊事件：GROUP_AT_MESSAGE_CREATE、C2C_MESSAGE_CREATE、FRIEND_ADD 等 (1&lt;&lt;25) */
    GROUP_AND_C2C_EVENT(1L << 25),
    /** 互动事件：INTERACTION_CREATE (1&lt;&lt;26) */
    INTERACTION(1L << 26),
    /** 消息审核事件：MESSAGE_AUDIT_PASS/REJECT (1&lt;&lt;27) */
    MESSAGE_AUDIT(1L << 27),
    /** 私域论坛事件：FORUM_THREAD/POST/REPLY_* (1&lt;&lt;28) */
    FORUMS_EVENT(1L << 28),
    /** 音频事件：AUDIO_START/FINISH/ON_MIC/OFF_MIC (1&lt;&lt;29) */
    AUDIO_ACTION(1L << 29),
    /** 公域频道消息事件：AT_MESSAGE_CREATE、PUBLIC_MESSAGE_DELETE (1&lt;&lt;30) */
    PUBLIC_GUILD_MESSAGES(1L << 30);

    private final long value;

    Intents(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    /** 多个 intent 位或组合。 */
    public static long of(Intents... intents) {
        long mask = 0;
        for (Intents intent : intents) {
            mask |= intent.value;
        }
        return mask;
    }
}