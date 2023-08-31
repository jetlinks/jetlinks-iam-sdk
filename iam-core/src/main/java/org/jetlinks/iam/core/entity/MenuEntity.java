package org.jetlinks.iam.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.api.crud.entity.GenericTreeSortSupportEntity;
import org.hswebframework.web.utils.DigestUtils;
import org.jetlinks.iam.core.configuration.ApiClientConfig;
import org.jetlinks.iam.core.enums.AccessSupportState;
import org.jetlinks.iam.core.service.PermissionCodec;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * 菜单信息.
 *
 * @author zhangji 2023/8/11
 */
@Getter
@Setter
public class MenuEntity extends GenericTreeSortSupportEntity<String> {

    private static final long serialVersionUID = 1L;

    /**
     * id需根据code, appId, owner生成
     *
     * @see MenuEntity#generateId()
     */
    @Schema(description = "菜单ID")
    @Size(max = 64)
    private String id;

    /**
     * 应用ID.
     *
     * @see ApiClientConfig#getClientId().
     */
    @Schema(description = "外部菜单所属应用ID")
    @Size(max = 64)
    private String appId;

    /**
     * 在多应用集成运行时使用此字段来区分菜单属于哪个系统
     * 具体标识由各应用前端进行定义
     */
    @Schema(description = "菜单所属系统ID")
    @NotBlank(message = "所属系统ID不能为空")
    @Size(max = 64)
    private String owner;

    @Schema(description = "名称")
    @Size(max = 64)
    private String name;

    @Schema(description = "编码。编码需在系统内唯一")
    @Size(max = 64)
    private String code;

    @Schema(description = "描述")
    @Size(max = 255)
    private String describe;

    @Schema(description = "URL,路由")
    @Size(max = 512)
    private String url;

    @Schema(description = "图标")
    @Size(max = 256)
    private String icon;

    @Schema(description = "绑定权限信息")
    private List<PermissionInfo> permissions;

    @Schema(description = "数据权限控制")
    private AccessSupportState accessSupport;

    @Schema(description = "关联菜单,accessSupport为indirect时不能为空")
    private List<String> indirectMenus;

    @Schema(description = "关联资产类型,accessSupport为support有值")
    private String assetType;

    @Schema(description = "按钮定义信息")
    private List<MenuButtonInfo> buttons;

    @Schema(description = "其他配置信息")
    private Map<String, Object> options;

    //子菜单
    @Schema(description = "子菜单")
    private List<MenuEntity> children;

    private boolean encoded;

    public void generateId() {
        String id = generateHexId(code, appId, owner);
        setId(id);
    }

    public static String generateHexId(String boardId, String appId, String owner) {
        return DigestUtils.md5Hex(String.join(boardId, "|", appId, "|", owner));
    }

    public boolean isSupportDataAccess() {
        return accessSupport != null
                && accessSupport != AccessSupportState.unsupported;
    }

    public boolean hasPermission(BiPredicate<String, Collection<String>> predicate) {
        if (CollectionUtils.isEmpty(permissions) && CollectionUtils.isEmpty(buttons)) {
            return false;
        }
        //有权限信息
        if (CollectionUtils.isNotEmpty(permissions)) {
            for (PermissionInfo permission : permissions) {
                if (!predicate.test(permission.getPermission(), permission.getActions())) {
                    return false;
                }
            }
            return true;
        }
        //有任意按钮信息
        if (CollectionUtils.isNotEmpty(buttons)) {
            for (MenuButtonInfo button : buttons) {
                if (button.hasPermission(predicate)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Optional<MenuButtonInfo> getButton(String id) {
        if (buttons == null) {
            return Optional.empty();
        }
        return buttons
                .stream()
                .filter(button -> Objects.equals(button.getId(), id))
                .findAny();
    }

    public MenuEntity init(PermissionCodec permissionCodec) {
        if (getId() == null) {
            generateId();
        }
        tryValidate();
        return encodePermission(permissionCodec);
    }

    /**
     * 编码权限ID
     * @param permissionCodec 编解码类
     * @return 菜单
     */
    public MenuEntity encodePermission(PermissionCodec permissionCodec) {
        if (this.encoded) {
            return this;
        }
        if (CollectionUtils.isNotEmpty(permissions)) {
            for (PermissionInfo permission : permissions) {
                permission.setPermission(permissionCodec.encode(permission.getPermission()));
            }
        }
        if (CollectionUtils.isNotEmpty(buttons)) {
            for (MenuButtonInfo button : buttons) {
                if (CollectionUtils.isNotEmpty(button.getPermissions())) {
                    for (PermissionInfo permission : button.getPermissions()) {
                        permission.setPermission(permissionCodec.encode(permission.getPermission()));
                    }
                }
            }
        }
        if (StringUtils.hasText(assetType)) {
            this.assetType = permissionCodec.encode(assetType);
        }
        this.encoded = true;

        return this;
    }

    /**
     * 解码权限ID
     * @param permissionCodec 编解码类
     * @return 菜单
     */
    public MenuEntity decodePermission(PermissionCodec permissionCodec) {
        if (CollectionUtils.isNotEmpty(permissions)) {
            for (PermissionInfo permission : permissions) {
                permission.setPermission(permissionCodec.decode(permission.getPermission()));
            }
        }
        if (CollectionUtils.isNotEmpty(buttons)) {
            for (MenuButtonInfo button : buttons) {
                if (CollectionUtils.isNotEmpty(button.getPermissions())) {
                    for (PermissionInfo permission : button.getPermissions()) {
                        permission.setPermission(permissionCodec.decode(permission.getPermission()));
                    }
                }
            }
        }
        if (StringUtils.hasText(assetType)) {
            this.assetType = permissionCodec.decode(assetType);
        }

        return this;
    }
}
