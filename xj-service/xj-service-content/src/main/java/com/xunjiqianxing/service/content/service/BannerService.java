package com.xunjiqianxing.service.content.service;

import com.xunjiqianxing.service.content.entity.Banner;

import java.util.List;

/**
 * 轮播图服务
 */
public interface BannerService {

    /**
     * 获取启用的轮播图列表
     */
    List<Banner> getActiveBanners();
}
