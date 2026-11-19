package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * 指令面板配置内容（官方 Panel 结构）。
 *
 * <p>与自定义菜单（MenuApi）的区别：面板是可滚动展示的指令/链接入口列表，按场景（c2c/group/channel/dm）
 * 与生效范围（全部 / 指定用户或群）分别配置；菜单只支持单聊且全局生效。一个机器人最多 20 个面板。</p>
 */
public class Panel {

    @JsonProperty("items")
    private List<PanelItem> items;

    /** 面板备注，最多 255 个字符，仅开发者可见 */
    @JsonProperty("remark")
    private String remark;

    /** 当前版本号，查询类响应中回填 */
    @JsonProperty("version")
    private Integer version;

    public static Panel of(List<PanelItem> items) {
        return new Panel().setItems(items);
    }

    public static Panel of(PanelItem... items) {
        return of(Arrays.asList(items));
    }

    public List<PanelItem> getItems() {
        return items;
    }

    public Panel setItems(List<PanelItem> items) {
        this.items = items;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public Panel setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public Panel setVersion(Integer version) {
        this.version = version;
        return this;
    }
}
