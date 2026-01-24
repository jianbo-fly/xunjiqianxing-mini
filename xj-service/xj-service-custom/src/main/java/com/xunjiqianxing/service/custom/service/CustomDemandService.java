package com.xunjiqianxing.service.custom.service;

import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.custom.entity.CustomDemand;

/**
 * 定制需求服务
 */
public interface CustomDemandService {

    /**
     * 提交定制需求
     */
    CustomDemand submit(CustomDemand demand);

    /**
     * 获取用户的定制需求列表
     */
    PageResult<CustomDemand> pageUserDemands(Long userId, PageQuery pageQuery);

    /**
     * 获取需求详情
     */
    CustomDemand getById(Long id);

    /**
     * 取消需求
     */
    boolean cancel(Long id, Long userId);
}
