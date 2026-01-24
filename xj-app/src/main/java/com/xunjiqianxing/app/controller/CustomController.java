package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.CustomDemandRequest;
import com.xunjiqianxing.app.dto.CustomDemandVO;
import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.custom.entity.CustomDemand;
import com.xunjiqianxing.service.custom.service.CustomDemandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定制游接口
 */
@RestController
@RequestMapping("/api/custom")
@RequiredArgsConstructor
@Tag(name = "定制游接口", description = "定制旅游需求相关接口")
public class CustomController {

    private final CustomDemandService customDemandService;

    // 状态映射
    private static final Map<Integer, String> STATUS_TEXT_MAP = new HashMap<>() {{
        put(0, "待处理");
        put(1, "跟进中");
        put(2, "已完成");
        put(3, "已关闭");
    }};

    /**
     * 提交定制需求
     */
    @PostMapping("/submit")
    @Operation(summary = "提交定制需求", description = "提交定制旅游需求")
    public Result<CustomDemandVO> submit(@Valid @RequestBody CustomDemandRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        CustomDemand demand = new CustomDemand();
        BeanUtils.copyProperties(request, demand);
        demand.setUserId(userId);

        CustomDemand created = customDemandService.submit(demand);
        return Result.success(toVO(created));
    }

    /**
     * 我的定制需求列表
     */
    @GetMapping("/list")
    @Operation(summary = "我的定制需求列表", description = "获取用户提交的定制需求列表")
    public Result<PageResult<CustomDemandVO>> list(PageQuery pageQuery) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageResult<CustomDemand> pageResult = customDemandService.pageUserDemands(userId, pageQuery);

        List<CustomDemandVO> voList = pageResult.getList().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(voList, pageResult.getTotal(),
                pageResult.getPage(), pageResult.getPageSize()));
    }

    /**
     * 定制需求详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "定制需求详情", description = "获取定制需求详细信息")
    public Result<CustomDemandVO> detail(
            @Parameter(description = "需求ID") @PathVariable Long id) {

        Long userId = StpUtil.getLoginIdAsLong();
        CustomDemand demand = customDemandService.getById(id);

        if (demand == null) {
            throw new BizException("需求不存在");
        }
        if (!demand.getUserId().equals(userId)) {
            throw new BizException("无权查看");
        }

        return Result.success(toVO(demand));
    }

    /**
     * 取消定制需求
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消定制需求", description = "取消待处理的定制需求")
    public Result<Void> cancel(
            @Parameter(description = "需求ID") @PathVariable Long id) {

        Long userId = StpUtil.getLoginIdAsLong();
        customDemandService.cancel(id, userId);
        return Result.success();
    }

    private CustomDemandVO toVO(CustomDemand demand) {
        CustomDemandVO vo = new CustomDemandVO();
        BeanUtils.copyProperties(demand, vo);
        vo.setStatusText(STATUS_TEXT_MAP.getOrDefault(demand.getStatus(), "未知"));
        return vo;
    }
}
