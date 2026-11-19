package cn.meilinkj.qqbot.core.event.guild;

/**
 * CHANNEL_CREATE（Intents.GUILDS）：子频道被创建时触发。调用 {@code client.channelManage().createChannel(...)}
 * 成功后平台也会下发本事件。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/channel_create.html">子频道创建</a>
 */
public class ChannelCreateEvent extends ChannelEvent {
}
