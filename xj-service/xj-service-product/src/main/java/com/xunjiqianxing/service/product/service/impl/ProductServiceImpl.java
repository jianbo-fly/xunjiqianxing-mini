package com.xunjiqianxing.service.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.mapper.ProductMainMapper;
import com.xunjiqianxing.service.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMainMapper productMainMapper;

    @Override
    public List<ProductMain> getRecommendProducts(int limit) {
        return productMainMapper.selectList(
                new LambdaQueryWrapper<ProductMain>()
                        .eq(ProductMain::getStatus, 1)
                        .eq(ProductMain::getIsDeleted, 0)
                        .eq(ProductMain::getAuditStatus, 1)
                        .eq(ProductMain::getIsRecommend, 1)
                        .orderByDesc(ProductMain::getSortOrder)
                        .last("LIMIT " + limit)
        );
    }

    @Override
    public List<ProductMain> getProductsByBizType(String bizType, int limit) {
        return productMainMapper.selectList(
                new LambdaQueryWrapper<ProductMain>()
                        .eq(ProductMain::getBizType, bizType)
                        .eq(ProductMain::getStatus, 1)
                        .eq(ProductMain::getIsDeleted, 0)
                        .eq(ProductMain::getAuditStatus, 1)
                        .orderByDesc(ProductMain::getSortOrder)
                        .last("LIMIT " + limit)
        );
    }

    @Override
    public ProductMain getById(Long id) {
        return productMainMapper.selectOne(
                new LambdaQueryWrapper<ProductMain>()
                        .eq(ProductMain::getId, id)
                        .eq(ProductMain::getIsDeleted, 0)
        );
    }

    @Override
    public void increaseViewCount(Long id) {
        productMainMapper.update(null,
                new LambdaUpdateWrapper<ProductMain>()
                        .eq(ProductMain::getId, id)
                        .setSql("view_count = view_count + 1")
        );
    }
}
