# Mock支付说明文档

> 由于微信支付证书未配置，当前使用Mock支付进行开发测试。本文档记录了Mock相关的修改，方便后续对接真实支付时还原。

## 修改的文件

### 1. 后端：PaymentServiceImpl.java

**文件路径**: `xj-service/xj-service-payment/src/main/java/com/xunjiqianxing/service/payment/service/impl/PaymentServiceImpl.java`

**修改内容**: `createPayment` 方法中注释了真实微信支付调用，使用Mock数据替代

**还原方法**: 删除Mock代码块，取消注释真实支付代码块

```java
// ============ 当前Mock代码（需要删除）============
// Mock支付数据
String mockPrepayId = "mock_prepay_" + System.currentTimeMillis();
record.setPrepayId(mockPrepayId);
// Mock模式：直接标记为已支付
record.setStatus(1);
record.setTransactionId("mock_txn_" + System.currentTimeMillis());
record.setPayTime(LocalDateTime.now());
paymentRecordMapper.insert(record);
log.info("创建支付成功(Mock): paymentNo={}, bizNo={}", paymentNo, bizNo);

// Mock模式：立即发布支付成功事件，更新订单状态
eventPublisher.publishEvent(new PaymentSuccessEvent(this, record));
log.info("Mock支付成功，已发布支付成功事件: paymentNo={}", paymentNo);

// 返回mock的小程序调起支付所需参数
Map<String, String> payParams = new java.util.HashMap<>();
payParams.put("paymentNo", paymentNo);
payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
payParams.put("nonceStr", java.util.UUID.randomUUID().toString().replace("-", ""));
payParams.put("package", "prepay_id=" + mockPrepayId);
payParams.put("signType", "RSA");
payParams.put("paySign", "mock_sign_" + System.currentTimeMillis());
return payParams;

// ============ 真实支付代码（需要取消注释）============
/*
try {
    WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
    request.setOutTradeNo(paymentNo);
    request.setDescription(description);
    request.setNotifyUrl(wxPayService.getConfig().getNotifyUrl());

    WxPayUnifiedOrderV3Request.Amount amountInfo = new WxPayUnifiedOrderV3Request.Amount();
    amountInfo.setTotal(amount.multiply(BigDecimal.valueOf(100)).intValue());
    amountInfo.setCurrency("CNY");
    request.setAmount(amountInfo);

    WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
    payer.setOpenid(openid);
    request.setPayer(payer);

    // 设置过期时间（微信要求RFC 3339格式，需要带时区偏移）
    request.setTimeExpire(record.getExpireAt()
            .atZone(ZoneId.of("Asia/Shanghai"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")));

    Object result = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, request);

    // 保存prepay_id
    if (result instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, String> resultMap = (Map<String, String>) result;
        record.setPrepayId(resultMap.get("prepay_id"));
    }

    paymentRecordMapper.insert(record);
    log.info("创建支付成功: paymentNo={}, bizNo={}", paymentNo, bizNo);

    // 返回小程序调起支付所需参数
    @SuppressWarnings("unchecked")
    Map<String, String> payParams = (Map<String, String>) result;
    payParams.put("paymentNo", paymentNo);
    return payParams;

} catch (WxPayException e) {
    log.error("创建微信支付失败: {}", e.getMessage(), e);
    throw new BizException("创建支付失败: " + e.getErrCodeDes());
}
*/
```

---

### 2. 小程序端：pay.js

**文件路径**: `xj-miniprogram/services/pay.js`

**修改内容**: `wxPay` 方法中增加了Mock支付检测，跳过真实微信支付调用

**还原方法**: 删除Mock检测代码块

```javascript
// ============ 当前Mock代码（需要删除）============
wxPay(payParams) {
  return new Promise((resolve, reject) => {
    // 检测是否是mock支付（开发环境）- 删除这段
    if (payParams.package && payParams.package.includes('mock_prepay_')) {
      console.log('检测到Mock支付，模拟支付成功');
      setTimeout(() => {
        resolve({ errMsg: 'requestPayment:ok (mock)' });
      }, 500);
      return;
    }
    // Mock检测代码结束

    wx.requestPayment({
      // ... 真实支付代码保持不变
    });
  });
}

// ============ 还原后的代码 ============
wxPay(payParams) {
  return new Promise((resolve, reject) => {
    wx.requestPayment({
      timeStamp: payParams.timeStamp,
      nonceStr: payParams.nonceStr,
      package: payParams.package,
      signType: payParams.signType || 'RSA',
      paySign: payParams.paySign,
      success: (res) => {
        resolve(res);
      },
      fail: (err) => {
        if (err.errMsg.includes('cancel')) {
          reject({ code: -2, message: '用户取消支付' });
        } else {
          reject({ code: -1, message: err.errMsg || '支付失败' });
        }
      }
    });
  });
}
```

---

## 对接真实支付的步骤

### 1. 配置微信支付证书

在 `application.yml` 或 `application-local.yml` 中配置：

```yaml
wx:
  pay:
    appId: ${WX_PAY_APP_ID}
    mchId: ${WX_PAY_MCH_ID}
    apiV3Key: ${WX_PAY_API_V3_KEY}
    privateKeyPath: /path/to/apiclient_key.pem
    privateCertPath: /path/to/apiclient_cert.pem
    notifyUrl: https://your-domain.com/notify/pay/wechat
```

### 2. 还原后端代码

编辑 `PaymentServiceImpl.java`：
1. 删除 Mock 代码块（约20行）
2. 取消注释真实支付代码块
3. 删除 `// TODO: 微信支付证书未配置` 注释

### 3. 还原小程序端代码

编辑 `services/pay.js`：
1. 删除 `wxPay` 方法中的 Mock 检测代码（约8行）

### 4. 测试

1. 确保微信支付证书配置正确
2. 确保回调地址可访问
3. 使用真实微信支付进行测试

---

## Mock支付流程说明

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   小程序     │────▶│   后端API   │────▶│  Mock处理   │
│  点击支付    │     │ /payment/   │     │             │
└─────────────┘     │  order/{id} │     │ 1.创建支付  │
                    └─────────────┘     │   记录     │
                                        │ 2.直接标记  │
                                        │   已支付   │
                                        │ 3.发布支付  │
                                        │   成功事件  │
                                        └──────┬──────┘
                                               │
                    ┌─────────────┐            │
                    │ 事件监听器   │◀───────────┘
                    │             │
                    │ 更新订单状态 │
                    │ 待支付→待确认│
                    └─────────────┘
                           │
┌─────────────┐            │
│   小程序     │◀───────────┘
│             │
│ 检测mock参数 │
│ 跳过wx.pay  │
│ 直接返回成功 │
└─────────────┘
```

---

*文档创建时间: 2026-02-03*
