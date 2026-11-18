package cn.meilinkj.qqbot.example;

import cn.meilinkj.qqbot.api.QQBotClient;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.event.friend.FriendAddEvent;
import cn.meilinkj.qqbot.core.event.group.GroupAddRobotEvent;
import cn.meilinkj.qqbot.core.event.lifecycle.ReadyEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.GroupAtMessageCreateEvent;
import cn.meilinkj.qqbot.core.gateway.Intents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 最小可运行 Demo：群@消息与单聊消息回声机器人。
 *
 * <p>运行前设置环境变量（凭证来自 QQ 开放平台 https://q.qq.com ，切勿硬编码）：</p>
 * <pre>
 * export QQBOT_APP_ID=你的AppID
 * export QQBOT_APP_SECRET=你的AppSecret
 * mvn -pl qqbot-sdk-example -am compile exec:java
 * </pre>
 *
 * <p>订阅 GROUP_AND_C2C_EVENT(1&lt;&lt;25)，被动回复走 msg_id + 自动递增 msg_seq。
 * 协议见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/">官方开发文档</a>。</p>
 */
public final class EchoBotDemo {

    private static final Logger log = LoggerFactory.getLogger(EchoBotDemo.class);

    private EchoBotDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        String appId = System.getenv("QQBOT_APP_ID");
        String appSecret = System.getenv("QQBOT_APP_SECRET");
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            log.error("请先设置环境变量 QQBOT_APP_ID 与 QQBOT_APP_SECRET（QQ 开放平台 https://q.qq.com 获取）");
            System.exit(1);
        }

        QQBotConfig config = QQBotConfig.builder(appId, appSecret)
                .intents(Intents.of(Intents.GROUP_AND_C2C_EVENT))
                .build();

        CountDownLatch shutdownLatch = new CountDownLatch(1);
        QQBotClient client = new QQBotClient(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.shutdown();
            shutdownLatch.countDown();
        }, "qqbot-shutdown"));

        client.on(ReadyEvent.class, e ->
                log.info("机器人已上线: {} (session={})", e.getUser().getUsername(), e.getSessionId()));

        client.on(GroupAtMessageCreateEvent.class, e -> {
            log.info("群@消息 group={} msgId={} content={}", e.getGroupOpenid(), e.getId(), e.getContent());
            client.group().replyText(e.getGroupOpenid(), e.getId(), "回声: " + e.getContent());
        });

        client.on(C2CMessageCreateEvent.class, e -> {
            String userOpenid = e.getAuthor().getUserOpenid();
            log.info("单聊消息 user={} msgId={} content={}", userOpenid, e.getId(), e.getContent());
            client.c2c().replyText(userOpenid, e.getId(), "回声: " + e.getContent());
        });

        client.on(GroupAddRobotEvent.class, e ->
                log.info("机器人被添加进群: group={}", e.getGroupOpenid()));

        client.on(FriendAddEvent.class, e ->
                log.info("新增好友: openid={}", e.getOpenid()));

        client.start();
        log.info("回声机器人已启动，等待消息中…（Ctrl+C 退出）");
        shutdownLatch.await();
    }
}