package com.example.demo.configs;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class VnpayConfig {
    @Value("${vnpay.url}")
    private String url;

    @Value("${vnpay.return.url}")
    private String returnUrl;

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.version}")
    private String version;

}
