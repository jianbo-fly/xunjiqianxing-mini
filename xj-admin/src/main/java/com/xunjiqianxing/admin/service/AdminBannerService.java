package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.content.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.content.entity.Banner;
import com.xunjiqianxing.service.content.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - Banner管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBannerService {

    private final BannerMapper bannerMapper;

    /**
     * 分页查询Banner列表
     */
    public PageResult<BannerVO> pageList(BannerQueryRequest request) {
        Page<Banner> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();

        // 标题
        if (StringUtils.hasText(request.getTitle())) {
            wrapper.like(Banner::getTitle, request.getTitle());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(Banner::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Banner::getSortOrder)
                .orderByDesc(Banner::getCreatedAt);

        Page<Banner> result = bannerMapper.selectPage(page, wrapper);

        List<BannerVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取所有启用的Banner
     */
    public List<BannerVO> listAll() {
        List<Banner> list = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .orderByDesc(Banner::getSortOrder)
        );
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 获取Banner详情
     */
    public BannerVO getDetail(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BizException("Banner不存在");
        }
        return convertToVO(banner);
    }

    /**
     * 创建Banner
     */
    public Long create(BannerCreateRequest request) {
        Banner banner = new Banner();
        BeanUtil.copyProperties(request, banner);
        banner.setId(IdGenerator.nextId());
        banner.setStatus(1); // 默认启用

        bannerMapper.insert(banner);
        log.info("创建Banner: id={}, title={}", banner.getId(), banner.getTitle());

        return banner.getId();
    }

    /**
     * 更新Banner
     */
    public void update(BannerUpdateRequest request) {
        Banner banner = bannerMapper.selectById(request.getId());
        if (banner == null) {
            throw new BizException("Banner不存在");
        }

        if (StringUtils.hasText(request.getTitle())) {
            banner.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getImageUrl())) {
            banner.setImageUrl(request.getImageUrl());
        }
        if (request.getLinkType() != null) {
            banner.setLinkType(request.getLinkType());
        }
        if (request.getLinkValue() != null) {
            banner.setLinkValue(request.getLinkValue());
        }
        if (request.getSortOrder() != null) {
            banner.setSortOrder(request.getSortOrder());
        }
        if (request.getStartTime() != null) {
            banner.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            banner.setEndTime(request.getEndTime());
        }

        bannerMapper.updateById(banner);
        log.info("更新Banner: id={}", request.getId());
    }

    /**
     * 启用/禁用Banner
     */
    public void updateStatus(Long id, Integer status) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BizException("Banner不存在");
        }

        banner.setStatus(status);
        bannerMapper.updateById(banner);

        log.info("更新Banner状态: id={}, status={}", id, status);
    }

    /**
     * 删除Banner
     */
    public void delete(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BizException("Banner不存在");
        }

        bannerMapper.deleteById(id);
        log.info("删除Banner: id={}", id);
    }

    /**
     * 调整排序
     */
    public void sort(SortRequest request) {
        int sortOrder = request.getIds().size();
        for (Long id : request.getIds()) {
            Banner banner = bannerMapper.selectById(id);
            if (banner != null) {
                banner.setSortOrder(sortOrder--);
                bannerMapper.updateById(banner);
            }
        }
        log.info("调整Banner排序: ids={}", request.getIds());
    }

    // ==================== 私有方法 ====================

    private BannerVO convertToVO(Banner banner) {
        BannerVO vo = new BannerVO();
        BeanUtil.copyProperties(banner, vo);
        vo.setLinkTypeDesc(getLinkTypeDesc(banner.getLinkType()));
        vo.setStatusDesc(banner.getStatus() == 1 ? "启用" : "禁用");
        return vo;
    }

    private String getLinkTypeDesc(Integer linkType) {
        if (linkType == null) return "无跳转";
        switch (linkType) {
            case 0: return "无跳转";
            case 1: return "线路详情";
            case 2: return "外部链接";
            case 3: return "小程序页面";
            default: return "";
        }
    }
}
