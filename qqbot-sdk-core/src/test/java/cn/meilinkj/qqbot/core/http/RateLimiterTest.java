package cn.meilinkj.qqbot.core.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void limitsAcquiresPerSecond() {
        RateLimiter limiter = new RateLimiter(5);
        long start = System.nanoTime();
        for (int i = 0; i < 11; i++) {
            limiter.acquire();
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        // 11 次获取、每秒 5 个 → 至少跨 2 个窗口
        assertTrue(elapsedMs >= 1000, "elapsed=" + elapsedMs);
    }

    @Test
    void withinQuotaIsImmediate() {
        RateLimiter limiter = new RateLimiter(100);
        long start = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            limiter.acquire();
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMs < 500, "elapsed=" + elapsedMs);
    }
}