package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.user.MemberExtendRequest;
import com.xunjiqianxing.admin.dto.user.MemberListVO;
import com.xunjiqianxing.admin.dto.user.MemberQueryRequest;
import com.xunjiqianxing.admin.service.AdminMemberService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会员管理接口
 */
@Tag(name = "会员管理")
@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class MemberController {

    private final AdminMemberService adminMemberService;

    @Operation(summary = "会员列表")
    @GetMapping("/list")
    public Result<PageResult<MemberListVO>> list(MemberQueryRequest request) {
        return Result.success(adminMemberService.pageList(request));
    }

    @Operation(summary = "延长会员时间")
    @PostMapping("/extend")
    public Result<Void> extend(@Valid @RequestBody MemberExtendRequest request) {
        adminMemberService.extend(request);
        return Result.success();
    }

    @Operation(summary = "会员统计")
    @GetMapping("/stats")
    public Result<AdminMemberService.MemberStatsVO> stats() {
        return Result.success(adminMemberService.getStats());
    }
}
