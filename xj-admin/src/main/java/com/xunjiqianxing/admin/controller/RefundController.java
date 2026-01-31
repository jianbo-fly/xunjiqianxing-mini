package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.order.RefundAuditRequest;
import com.xunjiqianxing.admin.dto.order.RefundListVO;
import com.xunjiqianxing.admin.dto.order.RefundQueryRequest;
import com.xunjiqianxing.admin.service.AdminRefundService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 退款管理接口
 */
@Tag(name = "退款管理")
@RestController
@RequestMapping("/admin/refund")
@RequiredArgsConstructor
public class RefundController {

    private final AdminRefundService adminRefundService;

    @Operation(summary = "退款列表")
    @GetMapping("/list")
    public Result<PageResult<RefundListVO>> list(RefundQueryRequest request) {
        return Result.success(adminRefundService.pageList(request));
    }

    @Operation(summary = "退款详情")
    @GetMapping("/{id}")
    public Result<RefundListVO> detail(@PathVariable Long id) {
        return Result.success(adminRefundService.getDetail(id));
    }

    @Operation(summary = "审核退款")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody RefundAuditRequest request) {
        adminRefundService.audit(request);
        return Result.success();
    }

    @Operation(summary = "待审核数量")
    @GetMapping("/pendingCount")
    public Result<Long> pendingCount() {
        return Result.success(adminRefundService.getPendingCount());
    }
}
