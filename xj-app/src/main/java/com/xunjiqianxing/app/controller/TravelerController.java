package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.TravelerRequest;
import com.xunjiqianxing.app.dto.TravelerVO;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.user.entity.UserTraveler;
import com.xunjiqianxing.service.user.service.TravelerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 出行人管理接口
 */
@RestController
@RequestMapping("/api/traveler")
@RequiredArgsConstructor
@Tag(name = "出行人接口", description = "出行人管理相关接口")
public class TravelerController {

    private final TravelerService travelerService;

    /**
     * 获取出行人列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取出行人列表", description = "获取当前用户的所有出行人")
    public Result<List<TravelerVO>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserTraveler> travelers = travelerService.listByUserId(userId);
        List<TravelerVO> voList = travelers.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取出行人详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取出行人详情", description = "获取出行人详细信息")
    public Result<TravelerVO> detail(
            @Parameter(description = "出行人ID") @PathVariable Long id) {

        Long userId = StpUtil.getLoginIdAsLong();
        UserTraveler traveler = travelerService.getById(id);
        if (traveler == null || !traveler.getUserId().equals(userId)) {
            return Result.fail("出行人不存在");
        }
        return Result.success(toVOWithFull(traveler));
    }

    /**
     * 添加出行人
     */
    @PostMapping("/add")
    @Operation(summary = "添加出行人", description = "添加新的出行人")
    public Result<TravelerVO> add(@Valid @RequestBody TravelerRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        UserTraveler traveler = new UserTraveler();
        traveler.setUserId(userId);
        traveler.setName(request.getName());
        traveler.setIdType(request.getIdType());
        traveler.setIdNo(request.getIdNo());
        traveler.setPhone(request.getPhone());
        traveler.setGender(request.getGender());
        traveler.setBirthday(request.getBirthday());
        traveler.setIsDefault(request.getIsDefault() != null && request.getIsDefault() ? 1 : 0);
        traveler.setEmergencyName(request.getEmergencyName());
        traveler.setEmergencyPhone(request.getEmergencyPhone());

        traveler = travelerService.add(traveler);
        return Result.success(toVO(traveler));
    }

    /**
     * 更新出行人
     */
    @PostMapping("/{id}/update")
    @Operation(summary = "更新出行人", description = "更新出行人信息")
    public Result<Void> update(
            @Parameter(description = "出行人ID") @PathVariable Long id,
            @Valid @RequestBody TravelerRequest request) {

        Long userId = StpUtil.getLoginIdAsLong();

        UserTraveler traveler = new UserTraveler();
        traveler.setId(id);
        traveler.setUserId(userId);
        traveler.setName(request.getName());
        traveler.setIdType(request.getIdType());
        traveler.setIdNo(request.getIdNo());
        traveler.setPhone(request.getPhone());
        traveler.setGender(request.getGender());
        traveler.setBirthday(request.getBirthday());
        traveler.setIsDefault(request.getIsDefault() != null && request.getIsDefault() ? 1 : 0);
        traveler.setEmergencyName(request.getEmergencyName());
        traveler.setEmergencyPhone(request.getEmergencyPhone());

        travelerService.update(traveler);
        return Result.success();
    }

    /**
     * 删除出行人
     */
    @PostMapping("/{id}/delete")
    @Operation(summary = "删除出行人", description = "删除指定的出行人")
    public Result<Void> delete(
            @Parameter(description = "出行人ID") @PathVariable Long id) {

        Long userId = StpUtil.getLoginIdAsLong();
        travelerService.delete(userId, id);
        return Result.success();
    }

    /**
     * 设为默认出行人
     */
    @PostMapping("/{id}/setDefault")
    @Operation(summary = "设为默认出行人", description = "将指定出行人设为默认")
    public Result<Void> setDefault(
            @Parameter(description = "出行人ID") @PathVariable Long id) {

        Long userId = StpUtil.getLoginIdAsLong();
        travelerService.setDefault(userId, id);
        return Result.success();
    }

    /**
     * 转换为VO（脱敏）
     */
    private TravelerVO toVO(UserTraveler traveler) {
        TravelerVO vo = new TravelerVO();
        vo.setId(traveler.getId());
        vo.setName(traveler.getName());
        vo.setIdType(traveler.getIdType());
        vo.setIdTypeDesc(TravelerVO.getIdTypeDesc(traveler.getIdType()));
        vo.setIdNo(TravelerVO.maskIdNo(traveler.getIdNo()));
        vo.setPhone(TravelerVO.maskPhone(traveler.getPhone()));
        vo.setGender(traveler.getGender());
        vo.setBirthday(traveler.getBirthday());
        vo.setIsDefault(traveler.getIsDefault() != null && traveler.getIsDefault() == 1);
        vo.setEmergencyName(traveler.getEmergencyName());
        vo.setEmergencyPhone(TravelerVO.maskPhone(traveler.getEmergencyPhone()));
        return vo;
    }

    /**
     * 转换为VO（完整信息，用于编辑）
     */
    private TravelerVO toVOWithFull(UserTraveler traveler) {
        TravelerVO vo = new TravelerVO();
        vo.setId(traveler.getId());
        vo.setName(traveler.getName());
        vo.setIdType(traveler.getIdType());
        vo.setIdTypeDesc(TravelerVO.getIdTypeDesc(traveler.getIdType()));
        vo.setIdNo(traveler.getIdNo()); // 完整证件号
        vo.setPhone(traveler.getPhone()); // 完整手机号
        vo.setGender(traveler.getGender());
        vo.setBirthday(traveler.getBirthday());
        vo.setIsDefault(traveler.getIsDefault() != null && traveler.getIsDefault() == 1);
        vo.setEmergencyName(traveler.getEmergencyName());
        vo.setEmergencyPhone(traveler.getEmergencyPhone()); // 完整电话
        return vo;
    }
}
