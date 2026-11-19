package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 群基本信息。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_info.get.html">获取群基本信息</a>
 */
public class GroupInfo {

    /** 群 openid */
    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 群名称 */
    @JsonProperty("group_name")
    private String groupName;

    /** 群简介（官方字段语义，非 QQ 群号码） */
    @JsonProperty("group_finger_memo")
    private String groupFingerMemo;

    /** 群分类文案 */
    @JsonProperty("group_class_text")
    private String groupClassText;

    /** 群标签列表 */
    @JsonProperty("group_tags")
    private List<String> groupTags;

    /** 群成员数量 */
    @JsonProperty("group_member_num")
    private Integer groupMemberNum;

    /** 跨应用关联群 openid，绑定三方应用时才有值 */
    @JsonProperty("union_group_openid")
    private String unionGroupOpenid;

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupFingerMemo() {
        return groupFingerMemo;
    }

    public String getGroupClassText() {
        return groupClassText;
    }

    public List<String> getGroupTags() {
        return groupTags;
    }

    public Integer getGroupMemberNum() {
        return groupMemberNum;
    }

    public String getUnionGroupOpenid() {
        return unionGroupOpenid;
    }
}
