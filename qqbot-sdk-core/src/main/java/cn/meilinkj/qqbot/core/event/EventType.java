package cn.meilinkj.qqbot.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.event.friend.FriendAddEvent;
import cn.meilinkj.qqbot.core.event.group.GroupAddRobotEvent;
import cn.meilinkj.qqbot.core.event.group.GroupDelRobotEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMsgReceiveEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMsgRejectEvent;
import cn.meilinkj.qqbot.core.event.interaction.InteractionCreateEvent;
import cn.meilinkj.qqbot.core.event.lifecycle.ReadyEvent;
import cn.meilinkj.qqbot.core.event.lifecycle.ResumedEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.ChannelMessageEvent;
import cn.meilinkj.qqbot.core.event.message.GroupAtMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.GroupMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.MessageAuditedEvent;
import cn.meilinkj.qqbot.core.event.message.MessageDeleteEvent;
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
 */
public enum EventType {

    READY(null, typed(ReadyEvent.class)),
    RESUMED(null, typed(ResumedEvent.class)),

    GUILD_CREATE(Intents.GUILDS, generic()),
    GUILD_UPDATE(Intents.GUILDS, generic()),
    GUILD_DELETE(Intents.GUILDS, generic()),
    CHANNEL_CREATE(Intents.GUILDS, generic()),
    CHANNEL_UPDATE(Intents.GUILDS, generic()),
    CHANNEL_DELETE(Intents.GUILDS, generic()),

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
    FRIEND_DEL(Intents.GROUP_AND_C2C_EVENT, generic()),
    C2C_MSG_REJECT(Intents.GROUP_AND_C2C_EVENT, generic()),
    C2C_MSG_RECEIVE(Intents.GROUP_AND_C2C_EVENT, generic()),
    GROUP_AT_MESSAGE_CREATE(Intents.GROUP_AND_C2C_EVENT, typed(GroupAtMessageCreateEvent.class)),
    GROUP_MESSAGE_CREATE(Intents.GROUP_AND_C2C_EVENT, typed(GroupMessageCreateEvent.class)),
    GROUP_ADD_ROBOT(Intents.GROUP_AND_C2C_EVENT, typed(GroupAddRobotEvent.class)),
    GROUP_DEL_ROBOT(Intents.GROUP_AND_C2C_EVENT, typed(GroupDelRobotEvent.class)),
    GROUP_MSG_REJECT(Intents.GROUP_AND_C2C_EVENT, typed(GroupMsgRejectEvent.class)),
    GROUP_MSG_RECEIVE(Intents.GROUP_AND_C2C_EVENT, typed(GroupMsgReceiveEvent.class)),

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