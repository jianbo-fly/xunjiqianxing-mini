package com.xunjiqianxing.service.product.service;

import com.xunjiqianxing.service.product.entity.ProductMain;

import java.util.List;

/**
 * 商品服务
 */
public interface ProductService {

    /**
     * 获取推荐商品列表
     */
    List<ProductMain> getRecommendProducts(int limit);

    /**
     * 根据业务类型获取商品列表
     */
    List<ProductMain> getProductsByBizType(String bizType, int limit);

    /**
     * 根据ID获取商品
     */
    ProductMain getById(Long id);

    /**
     * 增加浏览量
     */
    void increaseViewCount(Long id);
}
