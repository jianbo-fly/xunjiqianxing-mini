package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.order.*;
import com.xunjiqianxing.admin.service.AdminOrderService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理接口
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public Result<PageResult<OrderListVO>> list(OrderQueryRequest request) {
        return Result.success(adminOrderService.pageList(request));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.success(adminOrderService.getDetail(orderNo));
    }

    @Operation(summary = "确认/驳回订单")
    @PostMapping("/confirm")
    public Result<Void> confirmOrReject(@Valid @RequestBody OrderConfirmRequest request) {
        adminOrderService.confirmOrReject(request);
        return Result.success();
    }

    @Operation(summary = "添加备注")
    @PostMapping("/remark")
    public Result<Void> addRemark(@Valid @RequestBody OrderRemarkRequest request) {
        adminOrderService.addRemark(request);
        return Result.success();
    }

    @Operation(summary = "订单统计")
    @GetMapping("/stats")
    public Result<OrderStatsVO> stats() {
        return Result.success(adminOrderService.getStats());
    }
}
