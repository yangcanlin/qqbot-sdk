package cn.meilinkj.qqbot.core.event.guild;

/**
 * CHANNEL_DELETE（Intents.GUILDS）：子频道被删除时触发。调用 {@code client.channelManage().deleteChannel(...)}
 * 成功后平台也会下发本事件。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/channel_delete.html">子频道删除</a>
 */
public class ChannelDeleteEvent extends ChannelEvent {
}
