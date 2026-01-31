package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunjiqianxing.admin.dto.system.*;
import com.xunjiqianxing.admin.entity.SystemConfig;
import com.xunjiqianxing.admin.mapper.SystemConfigMapper;
import com.xunjiqianxing.common.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台 - 系统配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminConfigService {

    private final SystemConfigMapper configMapper;

    /**
     * 获取所有配置
     */
    public List<ConfigVO> listAll() {
        List<SystemConfig> list = configMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                        .orderByAsc(SystemConfig::getConfigKey)
        );
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取配置Map
     */
    public Map<String, String> getConfigMap() {
        List<SystemConfig> list = configMapper.selectList(null);
        Map<String, String> map = new HashMap<>();
        for (SystemConfig config : list) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    /**
     * 根据key获取配置值
     */
    public String getValue(String key) {
        SystemConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, key)
        );
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 更新配置
     */
    public void update(ConfigUpdateRequest request) {
        SystemConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, request.getConfigKey())
        );

        if (config == null) {
            // 创建新配置
            config = new SystemConfig();
            config.setId(IdGenerator.nextId());
            config.setConfigKey(request.getConfigKey());
            config.setConfigValue(request.getConfigValue());
            config.setConfigType(request.getConfigType());
            config.setRemark(request.getRemark());
            configMapper.insert(config);
            log.info("创建系统配置: key={}", request.getConfigKey());
        } else {
            // 更新配置
            config.setConfigValue(request.getConfigValue());
            if (request.getConfigType() != null) {
                config.setConfigType(request.getConfigType());
            }
            if (request.getRemark() != null) {
                config.setRemark(request.getRemark());
            }
            configMapper.updateById(config);
            log.info("更新系统配置: key={}", request.getConfigKey());
        }
    }

    /**
     * 批量更新配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(ConfigBatchUpdateRequest request) {
        for (ConfigUpdateRequest config : request.getConfigs()) {
            update(config);
        }
        log.info("批量更新系统配置: count={}", request.getConfigs().size());
    }

    /**
     * 删除配置
     */
    public void delete(String key) {
        configMapper.delete(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, key)
        );
        log.info("删除系统配置: key={}", key);
    }

    // ==================== 私有方法 ====================

    private ConfigVO convertToVO(SystemConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtil.copyProperties(config, vo);
        return vo;
    }

    // ==================== 预定义配置Key ====================

    /**
     * 会员价格（元/月）
     */
    public static final String KEY_MEMBER_PRICE = "member_price";

    /**
     * 领队申请费用（元）
     */
    public static final String KEY_LEADER_APPLY_FEE = "leader_apply_fee";

    /**
     * 推广员默认佣金比例
     */
    public static final String KEY_PROMOTER_DEFAULT_RATE = "promoter_default_rate";

    /**
     * 客服电话
     */
    public static final String KEY_SERVICE_PHONE = "service_phone";

    /**
     * 用户协议URL
     */
    public static final String KEY_USER_AGREEMENT_URL = "user_agreement_url";

    /**
     * 隐私政策URL
     */
    public static final String KEY_PRIVACY_POLICY_URL = "privacy_policy_url";

    /**
     * 关于我们
     */
    public static final String KEY_ABOUT_US = "about_us";
}
