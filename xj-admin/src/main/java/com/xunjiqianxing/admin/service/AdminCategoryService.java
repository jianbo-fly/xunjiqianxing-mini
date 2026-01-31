package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunjiqianxing.admin.dto.content.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.content.entity.Category;
import com.xunjiqianxing.service.content.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 分类管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 获取分类列表
     */
    public List<CategoryVO> list(String bizType) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .orderByDesc(Category::getSortOrder);

        if (StringUtils.hasText(bizType)) {
            wrapper.eq(Category::getBizType, bizType);
        }

        List<Category> list = categoryMapper.selectList(wrapper);

        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类详情
     */
    public CategoryVO getDetail(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }
        return convertToVO(category);
    }

    /**
     * 创建分类
     */
    public Long create(CategoryCreateRequest request) {
        // 检查名称是否重复
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getName, request.getName())
                        .eq(StringUtils.hasText(request.getBizType()),
                                Category::getBizType, request.getBizType())
        );
        if (count > 0) {
            throw new BizException("分类名称已存在");
        }

        Category category = new Category();
        BeanUtil.copyProperties(request, category);
        category.setId(IdGenerator.nextId());
        category.setStatus(1); // 默认启用

        categoryMapper.insert(category);
        log.info("创建分类: id={}, name={}", category.getId(), category.getName());

        return category.getId();
    }

    /**
     * 更新分类
     */
    public void update(CategoryUpdateRequest request) {
        Category category = categoryMapper.selectById(request.getId());
        if (category == null) {
            throw new BizException("分类不存在");
        }

        // 检查名称是否重复
        if (StringUtils.hasText(request.getName()) && !request.getName().equals(category.getName())) {
            Long count = categoryMapper.selectCount(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getName, request.getName())
                            .eq(StringUtils.hasText(category.getBizType()),
                                    Category::getBizType, category.getBizType())
                            .ne(Category::getId, request.getId())
            );
            if (count > 0) {
                throw new BizException("分类名称已存在");
            }
            category.setName(request.getName());
        }

        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getBizType() != null) {
            category.setBizType(request.getBizType());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        categoryMapper.updateById(category);
        log.info("更新分类: id={}", request.getId());
    }

    /**
     * 启用/禁用分类
     */
    public void updateStatus(Long id, Integer status) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }

        category.setStatus(status);
        categoryMapper.updateById(category);

        log.info("更新分类状态: id={}, status={}", id, status);
    }

    /**
     * 删除分类
     */
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }

        // TODO: 检查是否有关联数据

        categoryMapper.deleteById(id);
        log.info("删除分类: id={}", id);
    }

    /**
     * 调整排序
     */
    public void sort(SortRequest request) {
        int sortOrder = request.getIds().size();
        for (Long id : request.getIds()) {
            Category category = categoryMapper.selectById(id);
            if (category != null) {
                category.setSortOrder(sortOrder--);
                categoryMapper.updateById(category);
            }
        }
        log.info("调整分类排序: ids={}", request.getIds());
    }

    // ==================== 私有方法 ====================

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtil.copyProperties(category, vo);
        vo.setStatusDesc(category.getStatus() == 1 ? "启用" : "禁用");
        return vo;
    }
}
