package cn.meilinkj.qqbot.core.exception;

/**
 * SDK 统一运行时异常基类。
 */
public class QQBotException extends RuntimeException {

    public QQBotException(String message) {
        super(message);
    }

    public QQBotException(String message, Throwable cause) {
        super(message, cause);
    }
}