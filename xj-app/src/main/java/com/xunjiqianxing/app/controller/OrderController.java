package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.*;
import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.service.OrderService;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import com.xunjiqianxing.service.product.entity.ProductSku;
import com.xunjiqianxing.service.product.service.ProductService;
import com.xunjiqianxing.service.product.service.RouteService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单接口
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "订单接口", description = "订单管理相关接口")
public class OrderController {

    private final OrderService orderService;
    private final RouteService routeService;
    private final ProductService productService;
    private final UserService userService;


    /**
     * 创建订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "下单购买线路")
    public Result<OrderDetailVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 获取SKU信息
        ProductSku sku = routeService.getPackageById(request.getSkuId());
        if (sku == null) {
            throw new BizException("套餐不存在");
        }

        // 获取商品信息
        ProductMain product = productService.getById(sku.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }

        // 获取价格库存
        ProductPriceStock priceStock = routeService.getPriceStock(request.getSkuId(), request.getStartDate());
        if (priceStock == null || priceStock.getStatus() != 1) {
            throw new BizException("所选日期不可预订");
        }

        // 检查库存
        int totalCount = request.getAdultCount() + (request.getChildCount() != null ? request.getChildCount() : 0);
        int remaining = priceStock.getStock() - priceStock.getSold() - priceStock.getLocked();
        if (remaining < totalCount) {
            throw new BizException("库存不足");
        }

        // 计算价格
        BigDecimal adultTotal = priceStock.getPrice().multiply(BigDecimal.valueOf(request.getAdultCount()));
        BigDecimal childTotal = BigDecimal.ZERO;
        if (request.getChildCount() != null && request.getChildCount() > 0 && priceStock.getChildPrice() != null) {
            childTotal = priceStock.getChildPrice().multiply(BigDecimal.valueOf(request.getChildCount()));
        }
        BigDecimal totalAmount = adultTotal.add(childTotal);

        // TODO: 优惠券计算
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.subtract(discountAmount);

        // 获取用户绑定的推广员
        UserInfo user = userService.getById(userId);
        Long promoterId = user != null ? user.getBindpromoterId() : null;

        // 构建订单
        OrderMain order = new OrderMain();
        order.setBizType("route");
        order.setUserId(userId);
        order.setSupplierId(product.getSupplierId());
        order.setProductId(product.getId());
        order.setSkuId(sku.getId());
        order.setProductName(product.getName());
        order.setProductImage(product.getCoverImage());
        order.setSkuName(sku.getName());
        order.setStartDate(request.getStartDate());
        order.setAdultCount(request.getAdultCount());
        order.setChildCount(request.getChildCount() != null ? request.getChildCount() : 0);
        order.setAdultPrice(priceStock.getPrice());
        order.setChildPrice(priceStock.getChildPrice());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setCouponId(request.getCouponId());
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        order.setRemark(request.getRemark());
        order.setPromoterId(promoterId);

        // 构建出行人
        List<OrderTraveler> travelers = request.getTravelers().stream()
                .map(dto -> {
                    OrderTraveler traveler = new OrderTraveler();
                    traveler.setName(dto.getName());
                    traveler.setIdCard(dto.getIdCard()); // TODO: 加密存储
                    traveler.setPhone(dto.getPhone());
                    traveler.setTravelerType(dto.getTravelerType());
                    return traveler;
                })
                .collect(Collectors.toList());

        // 创建订单
        OrderMain createdOrder = orderService.createOrder(order, travelers);

        // TODO: 锁定库存

        return Result.success(toOrderDetailVO(createdOrder, travelers));
    }

    /**
     * 订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "订单列表", description = "获取用户订单列表")
    public Result<PageResult<OrderListVO>> list(
            @Parameter(description = "订单状态: 0待支付 1待确认 2已确认 3出行中 4已完成 5已取消 6退款申请中 7已退款 8已关闭")
            @RequestParam(required = false) Integer status,
            PageQuery pageQuery) {

        Long userId = StpUtil.getLoginIdAsLong();
        PageResult<OrderMain> pageResult = orderService.pageUserOrders(userId, status, pageQuery);

        List<OrderListVO> voList = pageResult.getList().stream()
                .map(this::toOrderListVO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(voList, pageResult.getTotal(),
                pageResult.getPage(), pageResult.getPageSize()));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "订单详情", description = "获取订单详细信息")
    public Result<OrderDetailVO> detail(
            @Parameter(description = "订单ID") @PathVariable Long id) {

        Long userId = StpUtil.getLoginIdAsLong();
        OrderMain order = orderService.getById(id);

        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException("无权查看此订单");
        }

        List<OrderTraveler> travelers = orderService.getOrderTravelers(id);
        return Result.success(toOrderDetailVO(order, travelers));
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消待支付的订单")
    public Result<Void> cancel(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam(required = false) String reason) {

        Long userId = StpUtil.getLoginIdAsLong();
        orderService.cancelOrder(id, userId, reason);
        return Result.success();
    }

    /**
     * 申请退款
     */
    @PostMapping("/{id}/refund")
    @Operation(summary = "申请退款", description = "申请订单退款")
    public Result<Void> refund(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "退款原因") @RequestParam String reason) {

        Long userId = StpUtil.getLoginIdAsLong();
        orderService.applyRefund(id, userId, reason);
        return Result.success();
    }

    // ========== 转换方法 ==========

    private OrderListVO toOrderListVO(OrderMain order) {
        OrderListVO vo = new OrderListVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(OrderStatus.getDesc(order.getStatus()));
        return vo;
    }

    private OrderDetailVO toOrderDetailVO(OrderMain order, List<OrderTraveler> travelers) {
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(OrderStatus.getDesc(order.getStatus()));

        // 转换出行人（脱敏处理）
        if (travelers != null) {
            vo.setTravelers(travelers.stream()
                    .map(t -> {
                        OrderDetailVO.TravelerVO tv = new OrderDetailVO.TravelerVO();
                        tv.setName(t.getName());
                        tv.setIdCard(maskIdCard(t.getIdCard()));
                        tv.setPhone(maskPhone(t.getPhone()));
                        tv.setTravelerType(t.getTravelerType());
                        return tv;
                    })
                    .collect(Collectors.toList()));
        }

        return vo;
    }

    /**
     * 身份证脱敏
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
