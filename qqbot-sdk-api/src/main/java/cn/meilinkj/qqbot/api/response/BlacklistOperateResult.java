package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 群黑名单操作结果。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.post.html">添加/删除群黑名单</a>
 */
public class BlacklistOperateResult {

    /** 操作失败的成员 openid 列表，全部成功时为空 */
    @JsonProperty("fail_openids")
    private List<String> failOpenids;

    public List<String> getFailOpenids() {
        return failOpenids;
    }
}
