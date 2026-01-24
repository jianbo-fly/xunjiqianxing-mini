package com.xunjiqianxing.service.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunjiqianxing.service.content.entity.Banner;
import com.xunjiqianxing.service.content.mapper.BannerMapper;
import com.xunjiqianxing.service.content.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现
 */
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;

    @Override
    public List<Banner> getActiveBanners() {
        LocalDateTime now = LocalDateTime.now();
        return bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .and(wrapper -> wrapper
                                .isNull(Banner::getStartTime)
                                .or()
                                .le(Banner::getStartTime, now)
                        )
                        .and(wrapper -> wrapper
                                .isNull(Banner::getEndTime)
                                .or()
                                .ge(Banner::getEndTime, now)
                        )
                        .orderByDesc(Banner::getSortOrder)
        );
    }
}
