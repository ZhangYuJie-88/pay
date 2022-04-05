package com.example.alipay.alipaydemo.property;

import lombok.Data;

/**
 * @author zhangyujie
 * @since 2022-04-05
 **/
@Data
public class AliProperties {

    /**
     * 应用id
     */
    public String appId;
    /**
     * 应用私钥
     */
    public String alipayPrivateKey;
    /**
     * 支付宝公钥
     */
    public String alipayPublicKey;
    /**
     * 字符编码
     */
    public String charset;
    /**
     * 签名方式
     */
    public String signType;
    /**
     * 数据格式
     */
    public String format;
    /**
     * 商家id
     */
    private String mchId;
    /**
     * 调用接口的url
     */
    private String serverUrl;
    /**
     * 支付回调url
     */
    private String notifyUrl;

    /**
     * pc支付前台通知
     */
    private String returnUrl;

    /**
     * 退款url
     */
    private String refundUrl;

}
