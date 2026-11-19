package cn.meilinkj.qqbot.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.event.friend.FriendAddEvent;
import cn.meilinkj.qqbot.core.event.friend.FriendDelEvent;
import cn.meilinkj.qqbot.core.event.group.GroupAddRobotEvent;
import cn.meilinkj.qqbot.core.event.group.GroupDelRobotEvent;
import cn.meilinkj.qqbot.core.event.group.GroupJoinRequestEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMemberAddEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMemberRemoveEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMsgReceiveEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMsgRejectEvent;
import cn.meilinkj.qqbot.core.event.guild.ChannelCreateEvent;
import cn.meilinkj.qqbot.core.event.guild.ChannelDeleteEvent;
import cn.meilinkj.qqbot.core.event.guild.ChannelUpdateEvent;
import cn.meilinkj.qqbot.core.event.guild.GuildCreateEvent;
import cn.meilinkj.qqbot.core.event.guild.GuildDeleteEvent;
import cn.meilinkj.qqbot.core.event.guild.GuildUpdateEvent;
import cn.meilinkj.qqbot.core.event.interaction.InteractionCreateEvent;
import cn.meilinkj.qqbot.core.event.lifecycle.ReadyEvent;
import cn.meilinkj.qqbot.core.event.lifecycle.ResumedEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMsgReceiveEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMsgRejectEvent;
import cn.meilinkj.qqbot.core.event.message.ChannelMessageEvent;
import cn.meilinkj.qqbot.core.event.message.GroupAtMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.GroupMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.MessageAuditedEvent;
import cn.meilinkj.qqbot.core.event.message.MessageDeleteEvent;
import cn.meilinkj.qqbot.core.event.message.SubscribeMessageStatusEvent;
import cn.meilinkj.qqbot.core.gateway.Intents;
import cn.meilinkj.qqbot.core.model.channel.Message;
import cn.meilinkj.qqbot.core.model.channel.MessageAudited;
import cn.meilinkj.qqbot.core.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 官方事件名（Payload.t）与 Intents、事件类型的映射。
 * 事件列表对齐
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html#事件订阅intents">事件订阅 Intents</a>。
 * 未强类型映射的事件以 {@link GenericEvent} 分发，原始数据可通过 getRaw() 获取。
 * 保留 generic() 的分组（GUILD_MEMBER_*、MESSAGE_REACTION_*、FORUM_*、AUDIO_*）在官方 api-v2 文档中
 * 只出现在「事件订阅 Intents」清单里，没有事件页、没有事件体字段规格，因此不凭空建模。
 */
public enum EventType {

    READY(null, typed(ReadyEvent.class)),
    RESUMED(null, typed(ResumedEvent.class)),

    GUILD_CREATE(Intents.GUILDS, typed(GuildCreateEvent.class)),
    GUILD_UPDATE(Intents.GUILDS, typed(GuildUpdateEvent.class)),
    GUILD_DELETE(Intents.GUILDS, typed(GuildDeleteEvent.class)),
    CHANNEL_CREATE(Intents.GUILDS, typed(ChannelCreateEvent.class)),
    CHANNEL_UPDATE(Intents.GUILDS, typed(ChannelUpdateEvent.class)),
    CHANNEL_DELETE(Intents.GUILDS, typed(ChannelDeleteEvent.class)),

    GUILD_MEMBER_ADD(Intents.GUILD_MEMBERS, generic()),
    GUILD_MEMBER_UPDATE(Intents.GUILD_MEMBERS, generic()),
    GUILD_MEMBER_REMOVE(Intents.GUILD_MEMBERS, generic()),

    MESSAGE_CREATE(Intents.GUILD_MESSAGES, channelMessage()),
    MESSAGE_DELETE(Intents.GUILD_MESSAGES, typed(MessageDeleteEvent.class)),

    MESSAGE_REACTION_ADD(Intents.GUILD_MESSAGE_REACTIONS, generic()),
    MESSAGE_REACTION_REMOVE(Intents.GUILD_MESSAGE_REACTIONS, generic()),

    DIRECT_MESSAGE_CREATE(Intents.DIRECT_MESSAGE, channelMessage()),
    DIRECT_MESSAGE_DELETE(Intents.DIRECT_MESSAGE, typed(MessageDeleteEvent.class)),

    C2C_MESSAGE_CREATE(Intents.GROUP_AND_C2C_EVENT, typed(C2CMessageCreateEvent.class)),
    FRIEND_ADD(Intents.GROUP_AND_C2C_EVENT, typed(FriendAddEvent.class)),
    FRIEND_DEL(Intents.GROUP_AND_C2C_EVENT, typed(FriendDelEvent.class)),
    C2C_MSG_REJECT(Intents.GROUP_AND_C2C_EVENT, typed(C2CMsgRejectEvent.class)),
    C2C_MSG_RECEIVE(Intents.GROUP_AND_C2C_EVENT, typed(C2CMsgReceiveEvent.class)),
    GROUP_AT_MESSAGE_CREATE(Intents.GROUP_AND_C2C_EVENT, typed(GroupAtMessageCreateEvent.class)),
    GROUP_MESSAGE_CREATE(Intents.GROUP_AND_C2C_EVENT, typed(GroupMessageCreateEvent.class)),
    GROUP_ADD_ROBOT(Intents.GROUP_AND_C2C_EVENT, typed(GroupAddRobotEvent.class)),
    GROUP_DEL_ROBOT(Intents.GROUP_AND_C2C_EVENT, typed(GroupDelRobotEvent.class)),
    GROUP_MSG_REJECT(Intents.GROUP_AND_C2C_EVENT, typed(GroupMsgRejectEvent.class)),
    GROUP_MSG_RECEIVE(Intents.GROUP_AND_C2C_EVENT, typed(GroupMsgReceiveEvent.class)),
    SUBSCRIBE_MESSAGE_STATUS(Intents.GROUP_AND_C2C_EVENT, typed(SubscribeMessageStatusEvent.class)),

    GROUP_MEMBER_ADD(Intents.GROUP_MEMBER_EVENT, typed(GroupMemberAddEvent.class)),
    GROUP_MEMBER_REMOVE(Intents.GROUP_MEMBER_EVENT, typed(GroupMemberRemoveEvent.class)),
    GROUP_JOIN_REQUEST(Intents.GROUP_MEMBER_EVENT, typed(GroupJoinRequestEvent.class)),

    INTERACTION_CREATE(Intents.INTERACTION, typed(InteractionCreateEvent.class)),

    MESSAGE_AUDIT_PASS(Intents.MESSAGE_AUDIT, audited()),
    MESSAGE_AUDIT_REJECT(Intents.MESSAGE_AUDIT, audited()),

    FORUM_THREAD_CREATE(Intents.FORUMS_EVENT, generic()),
    FORUM_THREAD_UPDATE(Intents.FORUMS_EVENT, generic()),
    FORUM_THREAD_DELETE(Intents.FORUMS_EVENT, generic()),
    FORUM_POST_CREATE(Intents.FORUMS_EVENT, generic()),
    FORUM_POST_DELETE(Intents.FORUMS_EVENT, generic()),
    FORUM_REPLY_CREATE(Intents.FORUMS_EVENT, generic()),
    FORUM_REPLY_DELETE(Intents.FORUMS_EVENT, generic()),
    FORUM_PUBLISH_AUDIT_RESULT(Intents.FORUMS_EVENT, generic()),

    AUDIO_START(Intents.AUDIO_ACTION, generic()),
    AUDIO_FINISH(Intents.AUDIO_ACTION, generic()),
    AUDIO_ON_MIC(Intents.AUDIO_ACTION, generic()),
    AUDIO_OFF_MIC(Intents.AUDIO_ACTION, generic()),

    AT_MESSAGE_CREATE(Intents.PUBLIC_GUILD_MESSAGES, channelMessage()),
    PUBLIC_MESSAGE_DELETE(Intents.PUBLIC_GUILD_MESSAGES, typed(MessageDeleteEvent.class));

    private static final Map<String, EventType> BY_NAME = new HashMap<>();

    static {
        for (EventType type : values()) {
            BY_NAME.put(type.name(), type);
        }
    }

    private final Intents intent;
    private final Function<JsonNode, BotEvent> factory;

    EventType(Intents intent, Function<JsonNode, BotEvent> factory) {
        this.intent = intent;
        this.factory = factory;
    }

    /** 该事件所属的 intent；READY/RESUMED 为网关生命周期事件，返回 null。 */
    public Intents getIntent() {
        return intent;
    }

    public BotEvent create(JsonNode d) {
        return factory.apply(d);
    }

    public static EventType fromName(String name) {
        return name == null ? null : BY_NAME.get(name);
    }

    private static Function<JsonNode, BotEvent> typed(Class<? extends BotEvent> clazz) {
        return d -> JsonUtil.fromJson(d == null ? JsonUtil.mapper().createObjectNode() : d, clazz);
    }

    private static Function<JsonNode, BotEvent> generic() {
        return d -> new GenericEvent();
    }

    private static Function<JsonNode, BotEvent> channelMessage() {
        return d -> {
            ChannelMessageEvent event = new ChannelMessageEvent();
            event.setMessage(JsonUtil.fromJson(d, Message.class));
            return event;
        };
    }

    private static Function<JsonNode, BotEvent> audited() {
        return d -> {
            MessageAuditedEvent event = new MessageAuditedEvent();
            event.setAudited(JsonUtil.fromJson(d, MessageAudited.class));
            return event;
        };
    }
}