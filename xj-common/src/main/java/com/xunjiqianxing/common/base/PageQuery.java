package com.xunjiqianxing.common.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页大小，默认10
     */
    private Integer pageSize = 10;

    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    /**
     * 校正分页参数
     */
    public void normalize() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
    }
}
