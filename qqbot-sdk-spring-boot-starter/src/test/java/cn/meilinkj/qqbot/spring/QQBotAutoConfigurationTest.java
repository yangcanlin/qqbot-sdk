package cn.meilinkj.qqbot.spring;

import cn.meilinkj.qqbot.api.QQBotClient;
import cn.meilinkj.qqbot.core.event.EventListener;
import cn.meilinkj.qqbot.core.event.message.GroupAtMessageCreateEvent;
import cn.meilinkj.qqbot.core.gateway.Intents;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starter 自动装配测试：属性绑定、QQBotClient 注册、监听器发现、条件装配。
 */
class QQBotAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QQBotAutoConfiguration.class));

    @Configuration(proxyBeanMethods = false)
    static class ListenerConfig {

        @Bean
        EventListener<GroupAtMessageCreateEvent> groupListener() {
            return new EventListener<>() {
                @Override
                public void onEvent(GroupAtMessageCreateEvent event) {
                }
            };
        }
    }

    @Test
    void registersClientWithBoundProperties() {
        runner.withPropertyValues(
                        "qqbot.app-id=10001",
                        "qqbot.app-secret=secret",
                        "qqbot.auto-start=false",
                        "qqbot.intents=" + ((1L << 25) | (1L << 26)),
                        "qqbot.requests-per-second=50")
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(QQBotClient.class);
                    QQBotClient client = context.getBean(QQBotClient.class);
                    assertThat(client.getConfig().getAppId()).isEqualTo("10001");
                    assertThat(client.getConfig().getIntents()).isEqualTo((1L << 25) | (1L << 26));
                    assertThat(client.getConfig().getRequestsPerSecond()).isEqualTo(50);
                });
    }

    @Test
    void defaultIntentsIsGroupAndC2C() {
        runner.withPropertyValues("qqbot.app-id=10001", "qqbot.app-secret=secret", "qqbot.auto-start=false")
                .run(context -> assertThat(context.getBean(QQBotClient.class).getConfig().getIntents())
                        .isEqualTo(Intents.GROUP_AND_C2C_EVENT.getValue()));
    }

    @Test
    void backsOffWithoutAppId() {
        runner.run(context -> assertThat(context).doesNotHaveBean(QQBotClient.class));
    }

    @Test
    void respectsUserProvidedClient() {
        runner.withPropertyValues("qqbot.app-id=10001", "qqbot.app-secret=secret", "qqbot.auto-start=false")
                .withBean(QQBotClient.class, () -> new QQBotClient(
                        cn.meilinkj.qqbot.core.QQBotConfig.builder("custom", "custom-secret").build()))
                .run(context -> {
                    assertThat(context).hasSingleBean(QQBotClient.class);
                    assertThat(context.getBean(QQBotClient.class).getConfig().getAppId()).isEqualTo("custom");
                });
    }
}