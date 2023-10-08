package org.jetlinks.iam.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

/**
 * 树结构实体.
 *
 * @author zhangji 2023/9/22
 */
@Getter
@Setter
public abstract class GenericTreeSortSupportEntity<PK> implements TreeSupportEntity<PK> {

    @Schema(description = "父节点ID")
    private PK parentId;

    /**
     * 树结构编码,用于快速查找, 每一层由4位字符组成,用-分割
     * 如第一层:0001 第二层:0001-0001 第三层:0001-0001-0001
     */
    @Schema(description = "树结构路径")
    @Size(max = 128)
    private String path;

    @Schema(description = "排序序号")
    private Long sortIndex;

    @Schema(description = "树层级")
    private Integer level;

}
