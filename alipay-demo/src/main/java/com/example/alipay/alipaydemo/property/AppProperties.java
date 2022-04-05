package com.example.alipay.alipaydemo.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangyujie
 * @since 2022-04-05
 **/
@Configuration
@ConfigurationProperties(prefix = "app.pay")
public class AppProperties {

    private AliProperties ali = new AliProperties();

    public AliProperties getAli() {
        return ali;
    }

    public void setAli(AliProperties ali) {
        this.ali = ali;
    }
}
