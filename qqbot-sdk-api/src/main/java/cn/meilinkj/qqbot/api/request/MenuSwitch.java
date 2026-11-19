package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 菜单开关配置（仅 MenuItem.TYPE_SWITCH 使用）。
 * 官方行为：用户切换开关后会触发一条消息，消息 ext 中以开关自身的 switch_id 为 key、1 为 value
 * （如 switch_id 为 "search" 时，打开后 ext 携带 "search=1"，关闭后不携带该标识），回调走消息链路而非互动事件。
 */
public class MenuSwitch {

    /** 开关唯一标识 */
    @JsonProperty("switch_id")
    private String switchId;

    /** 初始状态：true 默认打开 */
    @JsonProperty("default")
    private Boolean defaultOn;

    public static MenuSwitch of(String switchId, boolean defaultOn) {
        return new MenuSwitch().setSwitchId(switchId).setDefaultOn(defaultOn);
    }

    public String getSwitchId() {
        return switchId;
    }

    public MenuSwitch setSwitchId(String switchId) {
        this.switchId = switchId;
        return this;
    }

    public Boolean getDefaultOn() {
        return defaultOn;
    }

    public MenuSwitch setDefaultOn(Boolean defaultOn) {
        this.defaultOn = defaultOn;
        return this;
    }
}
