package com.example.alipay.alipaydemo.service;

import cn.hutool.core.util.RandomUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.example.alipay.alipaydemo.common.CommonMessage;
import com.example.alipay.alipaydemo.common.Result;
import com.example.alipay.alipaydemo.property.AppProperties;
import com.example.alipay.alipaydemo.utils.PaymentUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p> 支付宝支付
 *
 * @author zhangyujie
 * @since 2022-04-05
 **/
@Slf4j
@Service
public class AliPayService {

    @Resource
    private AppProperties appProperties;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 阿里 sdk 封装
     */
    private AlipayClient alipayClient;

    @PostConstruct
    public void initMethod() {
        alipayClient = new DefaultAlipayClient(
                appProperties.getAli().getServerUrl(),
                appProperties.getAli().getAppId(),
                appProperties.getAli().getAlipayPrivateKey(),
                appProperties.getAli().getFormat(),
                appProperties.getAli().getCharset(),
                appProperties.getAli().getAlipayPublicKey(),
                appProperties.getAli().getSignType());
    }

    /**
     * 支付宝提现
     *
     * @param amount
     */
    public Result deposit(Integer amount) {
        AlipayFundTransToaccountTransferModel transferModel = new AlipayFundTransToaccountTransferModel();
        transferModel.setOutBizNo(RandomUtil.randomNumbers(10));
        transferModel.setAmount("0.01");
        transferModel.setPayeeAccount("收款方账户");
        transferModel.setPayeeRealName("收款方真实姓名");
        transferModel.setPayerShowName("付款方姓名");
        transferModel.setRemark("remark");
        // ALIPAY_LOGONID：支付宝登录号，支持邮箱和手机号格式
        transferModel.setPayeeType("ALIPAY_LOGONID");
        try {
            AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
            request.setBizModel(transferModel);
            AlipayFundTransToaccountTransferResponse response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.info("ali deposit error message:{}", e.getMessage());
            return Result.success(CommonMessage.ALI_DEPOSIT_SUCCESS);
        }
        return Result.error(CommonMessage.ALI_DEPOSIT_FAILED);
    }


    /**
     * 支付宝扫码支付生成二维码响应到浏览器
     *
     * @param amount
     * @param response
     * @return
     */
    public void aliQrCodePay(Integer amount, HttpServletResponse response) throws Exception {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        Map<String, String> params = new TreeMap<>();
        params.put("out_trade_no", RandomUtil.randomNumbers(10));
        params.put("total_amount", amount.toString());
        params.put("subject", "备注");
        params.put("body", "详情");
        params.put("store_id", "NJ_2031");
        params.put("timeout_express", "90m");
        request.setBizContent(objectMapper.writeValueAsString(params));
        request.setNotifyUrl(appProperties.getAli().getNotifyUrl());

        AlipayTradePrecreateResponse responseData = alipayClient.execute(request);
        log.info("response:{}", responseData.getBody());
        String qrCode = responseData.getQrCode();
        if (!ObjectUtils.isEmpty(qrCode)) {
            PaymentUtils.createQRCode(qrCode, response);
        }
    }

    /**
     * 支付宝退款
     *
     * @param orderId
     * @param servletRequest
     * @return
     */
    public Result aliRefund(Long orderId, HttpServletRequest servletRequest) throws Exception {
        // 创建退款请求builder，设置请求参数
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        Map<String, String> params = new TreeMap<>();
        // 必须 商户订单号
        params.put("out_trade_no", "支付宝商户订单号");
        // 必须 支付宝交易号
        params.put("trade_no", "支付宝交易号");
        // 必须 退款金额
        params.put("refund_amount", "退款金额");
        // 可选 代表 退款的原因说明
        params.put("refund_reason", "退款的原因说明");
        // 可选 标识一次退款请求，同一笔交易多次退款需要保证唯一（就是out_request_no在2次退款一笔交易时，要不一样），如需部分退款，则此参数必传
        params.put("out_request_no", RandomUtil.randomNumbers(10));
        // 可选 代表 商户的门店编号
        params.put("store_id", "90m");
        request.setBizContent(objectMapper.writeValueAsString(params));
        AlipayTradeRefundResponse responseData = alipayClient.execute(request);
        if (responseData.isSuccess()) {
            log.info("ali refund success tradeNo:{}", "订单号");
            return Result.success(CommonMessage.SUCCESS);
        }
        log.info("ali refund failed tradeNo:{}", "订单号");
        return Result.error(CommonMessage.ALI_PAY_REFUND_FAILED);
    }


    /**
     * 阿里pc支付
     *
     * @param amount
     * @param servletRequest
     * @return
     */
    public String aliPcPay(Integer amount, HttpServletRequest servletRequest) throws Exception {
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        // 前台通知
        payRequest.setReturnUrl(appProperties.getAli().getReturnUrl());
        // 后台回调
        payRequest.setNotifyUrl(appProperties.getAli().getNotifyUrl());
        Map<String, String> params = new TreeMap<>();
        params.put("out_trade_no", "订单号");
        // 订单金额:元
        params.put("total_amount", amount.toString());
        params.put("subject", "订单标题");
        // 实际收款账号，一般填写商户PID即可
        params.put("seller_id", appProperties.getAli().getMchId());
        // 电脑网站支付
        params.put("product_code", "FAST_INSTANT_TRADE_PAY");
        params.put("body", "两个橘子");
        payRequest.setBizContent(objectMapper.writeValueAsString(params));
        log.info("业务参数:" + payRequest.getBizContent());
        String result = CommonMessage.ERROR.getMessage();
        try {
            result = alipayClient.pageExecute(payRequest).getBody();
        } catch (AlipayApiException e) {
            log.error("ali pay error message:{}", e.getMessage());
        }
        return result;
    }

    /**
     * 支付宝App支付
     *
     * @param amount
     * @param servletRequest
     * @return
     */
    public String appPay(Integer amount, HttpServletRequest servletRequest) {
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        // SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody("购买手机");
        model.setSubject("iPhone 13 pro max");
        model.setOutTradeNo(RandomUtil.randomNumbers(10));
        model.setTimeoutExpress("30m");
        model.setTotalAmount(amount.toString());
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(appProperties.getAli().getNotifyUrl());
        try {
            // 这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            //就是orderString 可以直接给客户端请求，无需再做处理。
            log.info("orderString:{}", response.getBody());
            return response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 支付回调通知
     *
     * @param request
     * @param response
     */
    public void aliNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestXml = PaymentUtils.getRequestData(request);
        Map requestMap = PaymentUtils.xmlToMap(requestXml);
        Assert.notNull(requestMap, CommonMessage.XML_DATA_INCORRECTNESS.getMessage());
        // 当返回的return_code为SUCCESS则回调成功
        if (requestMap.get("code").equals(10000)) {
            log.info("notify success");
        } else {
            log.error("notify failed");
        }
    }
}
