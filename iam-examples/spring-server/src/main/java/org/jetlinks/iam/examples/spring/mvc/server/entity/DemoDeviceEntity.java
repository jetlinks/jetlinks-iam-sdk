package org.jetlinks.iam.examples.spring.mvc.server.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

/**
 * 示例设备信息.
 *
 * @author zhangji 2023/8/8
 */
@Getter
@Setter
@Entity
@Table(name = "demo_device")
public class DemoDeviceEntity {

    @Id
    private String id;

    @Column
    @Schema(description = "设备名称")
    private String name;

    @Column
    @Schema(description = "类别名称")
    private String category;

    @Column
    @Schema(description = "存放位置")
    private String location;

    @Column
    @Schema(description = "管理员")
    private String manager;

    @Column
    @Schema(
            description = "创建者ID(只读)"
            , accessMode = Schema.AccessMode.READ_ONLY
    )
    private String creatorId;

    @Column
    @Schema(
            description = "创建者名称(只读)"
            , accessMode = Schema.AccessMode.READ_ONLY
    )
    private String creatorName;

    @Column
    @Schema(
            description = "创建时间(只读)"
            , accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long createTime;

    @Column
    @Schema(
            description = "修改时间"
            , accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long modifyTime;

    @Column
    @Schema(
            description = "修改人ID"
            , accessMode = Schema.AccessMode.READ_ONLY
    )
    private String modifierId;

    @Column
    @Schema(
            description = "修改人名称"
            , accessMode = Schema.AccessMode.READ_ONLY
    )
    private String modifierName;


}
