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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    /**
     * 企微客服链接
     */
    public static final String KEY_WECOM_SERVICE_URL = "wecom_service_url";

    /**
     * 支付超时时间（分钟）
     */
    public static final String KEY_PAYMENT_TIMEOUT = "payment_timeout";

    /**
     * 儿童年龄上限
     */
    public static final String KEY_CHILD_AGE_LIMIT = "child_age_limit";

    /**
     * 下单积分比例
     */
    public static final String KEY_ORDER_POINTS_RATE = "order_points_rate";

    /**
     * 推广积分比例
     */
    public static final String KEY_PROMOTER_POINTS_RATE = "promoter_points_rate";

    /**
     * 退款规则（JSON）
     */
    public static final String KEY_REFUND_RULES = "refund_rules";

    // ==================== 设置管理接口 ====================

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取基础设置
     */
    public BasicSettingsVO getBasicSettings() {
        BasicSettingsVO vo = new BasicSettingsVO();
        vo.setCustomerServicePhone(getValue(KEY_SERVICE_PHONE));
        vo.setWecomServiceUrl(getValue(KEY_WECOM_SERVICE_URL));

        String timeout = getValue(KEY_PAYMENT_TIMEOUT);
        vo.setPaymentTimeout(timeout != null ? Integer.parseInt(timeout) : 30);

        String ageLimit = getValue(KEY_CHILD_AGE_LIMIT);
        vo.setChildAgeLimit(ageLimit != null ? Integer.parseInt(ageLimit) : 12);

        return vo;
    }

    /**
     * 更新基础设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBasicSettings(BasicSettingsRequest request) {
        updateConfigValue(KEY_SERVICE_PHONE, request.getCustomerServicePhone());
        updateConfigValue(KEY_WECOM_SERVICE_URL, request.getWecomServiceUrl());
        updateConfigValue(KEY_PAYMENT_TIMEOUT, String.valueOf(request.getPaymentTimeout()));
        updateConfigValue(KEY_CHILD_AGE_LIMIT, String.valueOf(request.getChildAgeLimit()));
        log.info("更新基础设置");
    }

    /**
     * 获取积分规则设置
     */
    public PointsSettingsVO getPointsSettings() {
        PointsSettingsVO vo = new PointsSettingsVO();

        String orderRate = getValue(KEY_ORDER_POINTS_RATE);
        vo.setOrderPointsRate(orderRate != null ? new BigDecimal(orderRate) : new BigDecimal("1"));

        String promoterRate = getValue(KEY_PROMOTER_POINTS_RATE);
        vo.setPromoterPointsRate(promoterRate != null ? new BigDecimal(promoterRate) : new BigDecimal("5"));

        return vo;
    }

    /**
     * 更新积分规则设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePointsSettings(PointsSettingsRequest request) {
        updateConfigValue(KEY_ORDER_POINTS_RATE, request.getOrderPointsRate().toString());
        updateConfigValue(KEY_PROMOTER_POINTS_RATE, request.getPromoterPointsRate().toString());
        log.info("更新积分规则设置");
    }

    /**
     * 获取退款规则设置
     */
    public RefundSettingsVO getRefundSettings() {
        RefundSettingsVO vo = new RefundSettingsVO();

        String rulesJson = getValue(KEY_REFUND_RULES);
        if (rulesJson != null) {
            try {
                List<RefundRuleVO> rules = objectMapper.readValue(rulesJson, new TypeReference<List<RefundRuleVO>>() {});
                vo.setRules(rules);
            } catch (Exception e) {
                log.error("解析退款规则失败", e);
                vo.setRules(getDefaultRefundRules());
            }
        } else {
            vo.setRules(getDefaultRefundRules());
        }

        return vo;
    }

    /**
     * 更新退款规则设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRefundSettings(RefundSettingsRequest request) {
        try {
            String rulesJson = objectMapper.writeValueAsString(request.getRules());
            updateConfigValue(KEY_REFUND_RULES, rulesJson);
            log.info("更新退款规则设置");
        } catch (Exception e) {
            log.error("序列化退款规则失败", e);
            throw new RuntimeException("保存退款规则失败");
        }
    }

    /**
     * 获取价格设置
     */
    public PriceSettingsVO getPriceSettings() {
        PriceSettingsVO vo = new PriceSettingsVO();

        String memberPrice = getValue(KEY_MEMBER_PRICE);
        vo.setMemberPrice(memberPrice != null ? new BigDecimal(memberPrice) : new BigDecimal("99"));

        String leaderPrice = getValue(KEY_LEADER_APPLY_FEE);
        vo.setLeaderPrice(leaderPrice != null ? new BigDecimal(leaderPrice) : new BigDecimal("2000"));

        return vo;
    }

    /**
     * 更新价格设置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePriceSettings(PriceSettingsRequest request) {
        updateConfigValue(KEY_MEMBER_PRICE, request.getMemberPrice().toString());
        updateConfigValue(KEY_LEADER_APPLY_FEE, request.getLeaderPrice().toString());
        log.info("更新价格设置");
    }

    /**
     * 更新配置值
     */
    private void updateConfigValue(String key, String value) {
        ConfigUpdateRequest request = new ConfigUpdateRequest();
        request.setConfigKey(key);
        request.setConfigValue(value);
        update(request);
    }

    /**
     * 获取默认退款规则
     */
    private List<RefundRuleVO> getDefaultRefundRules() {
        List<RefundRuleVO> rules = new ArrayList<>();

        RefundRuleVO rule1 = new RefundRuleVO();
        rule1.setDaysBeforeStart(7);
        rule1.setRefundRate(new BigDecimal("100"));
        rules.add(rule1);

        RefundRuleVO rule2 = new RefundRuleVO();
        rule2.setDaysBeforeStart(3);
        rule2.setRefundRate(new BigDecimal("70"));
        rules.add(rule2);

        RefundRuleVO rule3 = new RefundRuleVO();
        rule3.setDaysBeforeStart(1);
        rule3.setRefundRate(new BigDecimal("50"));
        rules.add(rule3);

        return rules;
    }
}
