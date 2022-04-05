package com.example.alipay.alipaydemo.controller;

import com.example.alipay.alipaydemo.common.Result;
import com.example.alipay.alipaydemo.service.AliPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhangyujie
 * @since 2022-04-05
 **/
@Slf4j
@RestController
@RequestMapping(value = "/api/pay/ali")
public class AliPayController {

    @Resource
    private AliPayService aliPayService;

    /**
     * 支付宝App支付预下单
     *
     * @param amount
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/app")
    public Result appPay(Integer amount, HttpServletRequest request, HttpServletResponse response) {
        return Result.success(aliPayService.appPay(amount, request));
    }

    /**
     * 支付宝扫码支付预下单
     *
     * @param amount
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/qrcode")
    public void qrCodePay(Integer amount, HttpServletResponse response) throws Exception {
        aliPayService.aliQrCodePay(amount, response);
    }

    /**
     * 支付宝App支付退款
     * @param outTradeNo
     * @param tradeNo
     * @param amount
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/refund")
    public Result appRefund(String outTradeNo,String tradeNo,Integer amount,  HttpServletRequest request) throws Exception {
        return aliPayService.aliRefund(outTradeNo,tradeNo,amount, request);
    }


    /**
     * 支付宝支付通知
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.GET, RequestMethod.POST})
    public void aliNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        aliPayService.aliNotify(request, response);
    }


}
