package cn.meilinkj.qqbot.core.http;

/**
 * 简单客户端限流器（固定窗口，每秒 N 次）。
 * 官方各接口频控见
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/overview.html#频率与时效规则">频率与时效规则</a>，
 * 服务端 429 时由 {@link HttpApiClient} 退避重试兜底。
 */
public class RateLimiter {

    private final int permitsPerSecond;
    private long windowStartMs;
    private int used;

    public RateLimiter(int permitsPerSecond) {
        this.permitsPerSecond = Math.max(1, permitsPerSecond);
    }

    public synchronized void acquire() {
        long now = System.currentTimeMillis();
        if (now - windowStartMs >= 1000) {
            windowStartMs = now;
            used = 0;
        }
        if (used >= permitsPerSecond) {
            long waitMs = 1000 - (now - windowStartMs);
            if (waitMs > 0) {
                try {
                    wait(waitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("限流等待被中断", e);
                }
            }
            windowStartMs = System.currentTimeMillis();
            used = 0;
        }
        used++;
    }
}