package com.xunjiqianxing.service.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunjiqianxing.service.payment.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录Mapper
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}
