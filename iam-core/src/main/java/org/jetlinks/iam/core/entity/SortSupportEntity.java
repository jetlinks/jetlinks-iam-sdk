package org.jetlinks.iam.core.entity;

import javax.annotation.Nonnull;

/**
 * 排序实体.
 *
 * @author zhangji 2023/9/25
 */
public interface SortSupportEntity extends Comparable<SortSupportEntity> {

    /**
     * @return 排序序号
     */
    Long getSortIndex();

    /**
     * 设置排序序号
     *
     * @param sortIndex 排序序号
     */
    void setSortIndex(Long sortIndex);

    @Override
    default int compareTo(@Nonnull SortSupportEntity support) {
        return Long.compare(getSortIndex() == null ? 0 : getSortIndex(), support.getSortIndex() == null ? 0 : support.getSortIndex());
    }
}
