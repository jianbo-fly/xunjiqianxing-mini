package com.xunjiqianxing.service.order.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderRefund;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.entity.OrderLog;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Service 层单元测试基类
 * 使用 Mockito 隔离外部依赖，不启动 Spring 容器
 */
@ExtendWith(MockitoExtension.class)
public abstract class ServiceTestBase {

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration cfg = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(cfg, "");
        TableInfoHelper.initTableInfo(assistant, OrderMain.class);
        TableInfoHelper.initTableInfo(assistant, OrderTraveler.class);
        TableInfoHelper.initTableInfo(assistant, OrderRefund.class);
        TableInfoHelper.initTableInfo(assistant, OrderLog.class);
    }
}
