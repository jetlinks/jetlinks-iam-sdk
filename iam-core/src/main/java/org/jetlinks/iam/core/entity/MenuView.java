package org.jetlinks.iam.core.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.api.crud.entity.GenericTreeSortSupportEntity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.i18n.LocaleUtils;
import org.jetlinks.iam.core.enums.AccessSupportState;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 菜单信息.
 *
 * @author zhangji 2023/8/21
 */
@Getter
@Setter
public class MenuView extends GenericTreeSortSupportEntity<String> {

    /**
     * 集成其他应用的菜单时的应用ID.
     */
    @Schema(description = "外部菜单所属应用ID")
    private String appId;

    /**
     * 在多应用集成运行时使用此字段来区分菜单属于哪个系统
     * 具体标识由各应用前端进行定义
     */
    @Schema(description = "菜单所有者")
    private String owner;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "URL")
    private String url;

    @Schema(description = "父节点")
    private String parentId;

    @Schema(description = "按钮")
    private List<ButtonView> buttons;

    @Schema(description = "数据权限控制")
    @JSONField(name = "access_support")
    private AccessSupportState accessSupport;

    @Schema(description = "资产类型")
    private String assetType;

    @Schema(description = "关联菜单")
    private List<String> indirectMenus;

    @Schema(description = "资产数据权限控制")
    private List<AssetAccess> assetAccesses;

    @Schema(description = "其他配置")
    private Map<String, Object> options;

    @Schema(description = "子节点")
    private List<MenuView> children;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "数据权限说明")
    private String accessDescription;

    @Schema(description = "是否已授权")
    private boolean granted;

    public void removeUnGrantAccess() {
        if (CollectionUtils.isEmpty(assetAccesses)) {
            return;
        }
        int grantPriority = assetAccesses
                .stream()
                .filter(AssetAccess::isGranted)
                .map(AssetAccess::getPriority)
                .findFirst()
                .orElse(Integer.MAX_VALUE);

        assetAccesses.removeIf(access -> access.getPriority() > grantPriority);
    }

    @JsonIgnore
    public List<AssetAccess> getGrantedAssetAccesses() {
        return CollectionUtils.isEmpty(assetAccesses) ?
                Collections.emptyList() :
                assetAccesses
                        .stream()
                        .filter(AssetAccess::isGranted)
                        .collect(Collectors.toList());
    }

    public Optional<AssetAccess> getAssetAccess(String supportId) {
        if (CollectionUtils.isEmpty(assetAccesses)) {
            return Optional.empty();
        }
        return assetAccesses
                .stream()
                .filter(assetAccess -> Objects.equals(supportId, assetAccess.getSupportId()))
                .findFirst();
    }

    public MenuView withGranted(MenuView granted) {
        if (granted == null) {
            return this;
        }
        this.granted = true;

        this.options = granted.getOptions();
        return this
                .withGrantedButtons(granted.getButtons())
                .withGrantedAssetAccess(granted.getGrantedAssetAccesses());
    }

    public MenuView withAccessDescription(Locale locale,
                                          Function<String, MenuView> menuGetter) {
        if (accessSupport == null) {
            return this;
        }
        String i18nCode;
        Object[] args;
        String defaultMessage;
        if (accessSupport == AccessSupportState.indirect) {
            i18nCode = "menu.access.indirect.description";
            if (CollectionUtils.isNotEmpty(indirectMenus)) {
                args = new Object[1];
                args[0] = indirectMenus
                        .stream()
                        .map(menuGetter)
                        .filter(Objects::nonNull)
                        .map(MenuView::getName)
                        .collect(Collectors.joining(","));
                defaultMessage = "此菜单使用[" + args[0] + "]进行数据权限控制.";
            } else {
                args = new Object[0];
                defaultMessage = "此菜单使用其他功能进行数据权限控制.";
            }
        } else {
            i18nCode = "menu.access." + accessSupport.name() + ".description";
            args = new Object[0];
            defaultMessage = "此菜单" + accessSupport.getText() + "数据权限控制";
        }
        setAccessDescription(LocaleUtils.resolveMessage(i18nCode, locale, defaultMessage, args));
        return this;
    }

    public MenuView withGrantedAssetAccess(Collection<AssetAccess> accesses) {
        if (CollectionUtils.isEmpty(accesses) || CollectionUtils.isEmpty(this.assetAccesses)) {
            return this;
        }

        Map<String, AssetAccess> grantedAccesses =
                accesses
                        .stream()
                        .filter(AssetAccess::isGranted)
                        .collect(Collectors.toMap(AssetAccess::getSupportId, Function.identity(), (a, b) -> a));

        for (AssetAccess button : this.assetAccesses) {
            button.enabled = button.granted = grantedAccesses.containsKey(button.getSupportId());
        }
        return this;
    }

    /**
     * 设置已经赋权的按钮到当前菜单
     *
     * @param grantedButtons 全部按钮
     * @return 原始菜单
     */
    public MenuView withGrantedButtons(Collection<ButtonView> grantedButtons) {
        if (CollectionUtils.isEmpty(grantedButtons) || CollectionUtils.isEmpty(this.buttons)) {
            return this;
        }
        Map<String, ButtonView> grantedButtonMap =
                grantedButtons
                        .stream()
                        .collect(Collectors.toMap(ButtonView::getId, Function.identity(), (a, b) -> a));

        for (ButtonView button : this.buttons) {
            button.enabled = button.granted = grantedButtonMap.containsKey(button.getId());
        }
        return this;
    }

    public Optional<ButtonView> getButton(String id) {
        if (CollectionUtils.isEmpty(buttons)) {
            return Optional.empty();
        }
        return buttons
                .stream()
                .filter(button -> Objects.equals(id, button.getId()))
                .findFirst();
    }

    public void grantAll() {
        this.granted = true;
        if (CollectionUtils.isNotEmpty(getButtons())) {
            for (ButtonView button : getButtons()) {
                button.granted = true;
            }
        }

    }

    public void resetGrant() {
        this.granted = false;
        if (CollectionUtils.isNotEmpty(getButtons())) {
            for (AssetAccess access : getAssetAccesses()) {
                access.granted = false;
            }
        }
        if (CollectionUtils.isNotEmpty(getButtons())) {
            for (ButtonView button : getButtons()) {
                button.granted = false;
            }
        }
    }

    @Setter
    @Getter
    public static class AssetAccess implements Serializable {

        private static final long serialVersionUID = 1;

        @Schema(description = "数据支持类型")
        @NotBlank
        private String supportId;

        @Schema(description = "数据权限控制名称")
        private String name;
        /**
         * 是否启用
         */
        @Schema(description = "是否启用")
        @Deprecated
        private boolean enabled;

        @Schema(description = "是否已授权")
        private boolean granted;

        private int priority;

        public Map<String, Object> toMap() {
            return FastBeanCopier.copy(this, HashMap::new);
        }

        public AssetAccess merge(AssetAccess access) {

            return this;
        }

        private AssetAccess(String supportId, String name, boolean granted) {
            setSupportId(supportId);
            this.name = name;
            this.granted = granted;
        }

        public AssetAccess() {
        }

        public static AssetAccess of(String supportId, String name, boolean granted) {
            return new AssetAccess(supportId, name, granted);
        }

        public AssetAccess copy() {
            return FastBeanCopier.copy(this, new AssetAccess());
        }

        public AssetAccess grant() {
            this.granted = true;
            return this;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class ButtonView implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "按钮ID")
        private String id;

        @Schema(description = "按钮名称")
        private String name;

        @Schema(description = "说明")
        private String description;

        @Schema(description = "其他配置")
        private Map<String, Object> options;

        @Schema(description = "是否启用")
        @Deprecated
        private boolean enabled;

        @Schema(description = "是否已授权")
        private boolean granted;

        public static ButtonView of(String id, String name, String description, Map<String, Object> options) {
            return ButtonView.of(id, name, description, options, true, true);
        }

        public static ButtonView copy(ButtonView button) {
            return ButtonView.of(button.getId(), button.getName(), button.getDescription(), button.getOptions());
        }


        public ButtonView copy() {
            return FastBeanCopier.copy(this, new ButtonView());
        }
    }

    public static MenuView of(MenuEntity entity) {
        return FastBeanCopier.copy(entity, new MenuView());
    }

}

