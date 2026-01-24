package com.xunjiqianxing.service.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 价格库存 Mapper
 */
@Mapper
public interface ProductPriceStockMapper extends BaseMapper<ProductPriceStock> {

    /**
     * 获取SKU在日期范围内的价格库存
     */
    List<ProductPriceStock> selectBySkuIdAndDateRange(
            @Param("skuId") Long skuId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
