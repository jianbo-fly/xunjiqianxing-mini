package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.MemberInfoVO;
import com.xunjiqianxing.app.dto.MemberOrderVO;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.member.entity.MemberOrder;
import com.xunjiqianxing.service.member.service.MemberService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 会员接口
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "会员接口", description = "会员相关接口")
public class MemberController {

    private final MemberService memberService;
    private final UserService userService;

    /**
     * 获取会员信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取会员信息", description = "获取当前用户的会员信息")
    public Result<MemberInfoVO> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);

        MemberInfoVO vo = MemberInfoVO.builder()
                .isMember(user.getIsMember() != null && user.getIsMember() == 1)
                .expireAt(user.getMemberExpireAt())
                .expireDesc(memberService.getMemberExpireDesc(userId))
                .yearPrice(memberService.getMemberPrice())
                .build();

        return Result.success(vo);
    }

    /**
     * 购买会员
     */
    @PostMapping("/buy")
    @Operation(summary = "购买会员", description = "创建会员购买订单")
    public Result<MemberOrderVO> buy(
            @Parameter(description = "购买月数，默认12") @RequestParam(defaultValue = "12") Integer months) {

        Long userId = StpUtil.getLoginIdAsLong();
        MemberOrder order = memberService.createOrder(userId, months);

        MemberOrderVO vo = new MemberOrderVO();
        BeanUtils.copyProperties(order, vo);
        return Result.success(vo);
    }

    /**
     * 会员权益说明
     */
    @GetMapping("/benefits")
    @Operation(summary = "会员权益说明", description = "获取会员权益说明")
    public Result<Object> benefits() {
        // 返回会员权益说明
        return Result.success(new Object() {
            public String title = "寻迹千行会员权益";
            public String[] benefits = {
                    "全场线路享会员价",
                    "专属客服优先服务",
                    "积分双倍返还",
                    "会员专享优惠券",
                    "免费行程规划服务"
            };
            public String price = "99元/年";
        });
    }
}
