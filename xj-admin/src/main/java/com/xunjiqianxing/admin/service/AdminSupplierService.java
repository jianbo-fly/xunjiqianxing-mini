package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.system.*;
import com.xunjiqianxing.admin.entity.SystemSupplier;
import com.xunjiqianxing.admin.mapper.SystemSupplierMapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import cn.hutool.crypto.digest.BCrypt;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.mapper.ProductMainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 供应商管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSupplierService {

    private final SystemSupplierMapper supplierMapper;
    private final ProductMainMapper productMainMapper;
    private final OrderMainMapper orderMainMapper;

    /**
     * 分页查询供应商列表
     */
    public PageResult<SupplierVO> pageList(SupplierQueryRequest request) {
        Page<SystemSupplier> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<SystemSupplier> wrapper = new LambdaQueryWrapper<>();

        // 名称
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(SystemSupplier::getName, request.getName());
        }

        // 电话
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(SystemSupplier::getPhone, request.getPhone());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(SystemSupplier::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(SystemSupplier::getCreatedAt);

        Page<SystemSupplier> result = supplierMapper.selectPage(page, wrapper);

        List<SupplierVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取供应商详情
     */
    public SupplierVO getDetail(Long id) {
        SystemSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }
        return convertToVO(supplier);
    }

    /**
     * 创建供应商
     */
    public Long create(SupplierCreateRequest request) {
        // 检查账号是否重复
        Long count = supplierMapper.selectCount(
                new LambdaQueryWrapper<SystemSupplier>()
                        .eq(SystemSupplier::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BizException("登录账号已存在");
        }

        SystemSupplier supplier = new SystemSupplier();
        BeanUtil.copyProperties(request, supplier);
        supplier.setId(IdGenerator.nextId());
        supplier.setPassword(BCrypt.hashpw(request.getPassword()));
        supplier.setStatus(1); // 默认正常

        supplierMapper.insert(supplier);
        log.info("创建供应商: id={}, name={}", supplier.getId(), supplier.getName());

        return supplier.getId();
    }

    /**
     * 更新供应商
     */
    public void update(SupplierUpdateRequest request) {
        SystemSupplier supplier = supplierMapper.selectById(request.getId());
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }

        if (StringUtils.hasText(request.getName())) {
            supplier.setName(request.getName());
        }
        if (request.getLogo() != null) {
            supplier.setLogo(request.getLogo());
        }
        if (StringUtils.hasText(request.getPhone())) {
            supplier.setPhone(request.getPhone());
        }
        if (request.getIntro() != null) {
            supplier.setIntro(request.getIntro());
        }
        if (request.getLicenseImages() != null) {
            supplier.setLicenseImages(request.getLicenseImages());
        }

        supplierMapper.updateById(supplier);
        log.info("更新供应商: id={}", request.getId());
    }

    /**
     * 启用/禁用供应商
     */
    public void updateStatus(Long id, Integer status) {
        SystemSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }

        supplier.setStatus(status);
        supplierMapper.updateById(supplier);

        log.info("更新供应商状态: id={}, status={}", id, status);
    }

    /**
     * 重置密码
     */
    public String resetPassword(Long id) {
        SystemSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }

        // 生成随机密码
        String newPassword = generateRandomPassword();
        supplier.setPassword(BCrypt.hashpw(newPassword));
        supplierMapper.updateById(supplier);

        log.info("重置供应商密码: id={}", id);

        return newPassword;
    }

    /**
     * 删除供应商
     */
    public void delete(Long id) {
        SystemSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BizException("供应商不存在");
        }

        // 检查是否有关联数据
        Long routeCount = productMainMapper.selectCount(
                new LambdaQueryWrapper<ProductMain>()
                        .eq(ProductMain::getSupplierId, id)
        );
        if (routeCount > 0) {
            throw new BizException("该供应商有关联线路，无法删除");
        }

        supplierMapper.deleteById(id);
        log.info("删除供应商: id={}", id);
    }

    // ==================== 私有方法 ====================

    private SupplierVO convertToVO(SystemSupplier supplier) {
        SupplierVO vo = new SupplierVO();
        BeanUtil.copyProperties(supplier, vo);
        vo.setStatusDesc(supplier.getStatus() == 1 ? "正常" : "禁用");

        // 统计线路数
        Long routeCount = productMainMapper.selectCount(
                new LambdaQueryWrapper<ProductMain>()
                        .eq(ProductMain::getSupplierId, supplier.getId())
        );
        vo.setRouteCount(routeCount.intValue());

        // 统计订单数
        Long orderCount = orderMainMapper.selectCount(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getSupplierId, supplier.getId())
        );
        vo.setOrderCount(orderCount.intValue());

        return vo;
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
