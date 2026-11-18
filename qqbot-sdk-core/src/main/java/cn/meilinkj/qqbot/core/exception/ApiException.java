package cn.meilinkj.qqbot.core.exception;

/**
 * OpenAPI 业务错误。错误结构对齐官方文档：
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/error/error.html">错误与调试</a>
 *
 * <p>HTTP body 形如 {@code {"err_code":40034005,"message":"...","trace_id":"..."}}，
 * 部分接口使用 {@code code} 字段。trace_id 也可从响应头 {@code X-Tps-trace-ID} 获取。</p>
 */
public class ApiException extends QQBotException {

    private final int httpStatus;
    private final long errCode;
    private final String traceId;
    private final String rawBody;

    public ApiException(int httpStatus, long errCode, String message, String traceId, String rawBody) {
        super("QQ OpenAPI 错误 http=" + httpStatus + " err_code=" + errCode
                + " message=" + message + " trace_id=" + traceId);
        this.httpStatus = httpStatus;
        this.errCode = errCode;
        this.traceId = traceId;
        this.rawBody = rawBody;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public long getErrCode() {
        return errCode;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getRawBody() {
        return rawBody;
    }

    /**
     * 304023/304024：推送/回复消息异步调用成功，等待人工审核（HTTP 201/202）。
     * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/send.html#错误码">发送子频道消息-错误码</a>
     */
    public boolean isPendingAudit() {
        return errCode == 304023 || errCode == 304024;
    }

    /** 429：频率限制。 */
    public boolean isRateLimited() {
        return httpStatus == 429;
    }
}