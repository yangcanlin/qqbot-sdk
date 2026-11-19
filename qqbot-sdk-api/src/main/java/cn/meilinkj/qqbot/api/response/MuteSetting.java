package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 群禁言状态（全局禁言规则 + 被禁言成员列表）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.get.html">查询群禁言状态</a>
 */
public class MuteSetting {

    /** 全局禁言规则 */
    @JsonProperty("global_rule")
    private GlobalRule globalRule;

    /** 当前被禁言的成员 */
    @JsonProperty("members")
    private List<MutedMember> members;

    public GlobalRule getGlobalRule() {
        return globalRule;
    }

    public List<MutedMember> getMembers() {
        return members;
    }

    /** 全局禁言规则：mode=none 关闭 / always 长期开启 / schedule 定时。 */
    public static class GlobalRule {

        /** none / always / schedule */
        @JsonProperty("mode")
        private String mode;

        /** 定时禁言任务（一次性） */
        @JsonProperty("schedule_rules")
        private List<ScheduleRule> scheduleRules;

        /** 周期禁言任务 */
        @JsonProperty("recurring_rules")
        private List<RecurringRule> recurringRules;

        public String getMode() {
            return mode;
        }

        public List<ScheduleRule> getScheduleRules() {
            return scheduleRules;
        }

        public List<RecurringRule> getRecurringRules() {
            return recurringRules;
        }
    }

    /** 一次性定时禁言任务。 */
    public static class ScheduleRule {

        @JsonProperty("task_id")
        private String taskId;

        /** 开始时间（RFC3339 格式） */
        @JsonProperty("start_at")
        private String startAt;

        /** 结束时间（RFC3339 格式） */
        @JsonProperty("end_at")
        private String endAt;

        @JsonProperty("enabled")
        private Boolean enabled;

        public String getTaskId() {
            return taskId;
        }

        public String getStartAt() {
            return startAt;
        }

        public String getEndAt() {
            return endAt;
        }

        public Boolean getEnabled() {
            return enabled;
        }
    }

    /** 周期性禁言任务。 */
    public static class RecurringRule {

        @JsonProperty("task_id")
        private String taskId;

        /** 生效星期：1~7（周一至周日） */
        @JsonProperty("weekdays")
        private List<Integer> weekdays;

        /** 每日开始时间，HH:mm 格式 */
        @JsonProperty("start_time")
        private String startTime;

        /** 每日结束时间，HH:mm 格式 */
        @JsonProperty("end_time")
        private String endTime;

        @JsonProperty("enabled")
        private Boolean enabled;

        public String getTaskId() {
            return taskId;
        }

        public List<Integer> getWeekdays() {
            return weekdays;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public Boolean getEnabled() {
            return enabled;
        }
    }

    /** 被禁言成员及其禁言到期时间。 */
    public static class MutedMember {

        @JsonProperty("member_openid")
        private String memberOpenid;

        /** 禁言到期时间（RFC3339 格式） */
        @JsonProperty("mute_expire_at")
        private String muteExpireAt;

        @JsonProperty("username")
        private String username;

        @JsonProperty("union_openid")
        private String unionOpenid;

        public String getMemberOpenid() {
            return memberOpenid;
        }

        public String getMuteExpireAt() {
            return muteExpireAt;
        }

        public String getUsername() {
            return username;
        }

        public String getUnionOpenid() {
            return unionOpenid;
        }
    }
}
