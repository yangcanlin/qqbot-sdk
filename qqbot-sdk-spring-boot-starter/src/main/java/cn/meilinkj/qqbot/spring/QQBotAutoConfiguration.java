package cn.meilinkj.qqbot.spring;

import cn.meilinkj.qqbot.api.QQBotClient;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.event.EventListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot 3 自动装配：
 * <ul>
 *   <li>按 qqbot.* 属性构建 {@link QQBotClient}（容器关闭时自动 shutdown）</li>
 *   <li>自动注册容器内所有 {@link EventListener} Bean</li>
 *   <li>qqbot.auto-start=true（默认）时应用就绪后自动连接网关</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(QQBotProperties.class)
@ConditionalOnProperty(prefix = "qqbot", name = "app-id")
public class QQBotAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public QQBotClient qqBotClient(QQBotProperties properties, ObjectProvider<EventListener<?>> listeners) {
        QQBotConfig.Builder builder = QQBotConfig.builder(properties.getAppId(), properties.getAppSecret())
                .intents(properties.getIntents())
                .shard(properties.getShardId(), properties.getNumShards())
                .maxRetries(properties.getMaxRetries())
                .requestsPerSecond(properties.getRequestsPerSecond())
                .eventThreads(properties.getEventThreads());
        if (properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank()) {
            builder.baseUrl(properties.getBaseUrl());
        }
        QQBotClient client = new QQBotClient(builder.build());
        listeners.orderedStream().forEach(client::on);
        return client;
    }

    @Bean
    public ApplicationRunner qqBotGatewayRunner(QQBotClient client, QQBotProperties properties) {
        return args -> {
            if (properties.isAutoStart()) {
                client.start();
            }
        };
    }
}