package com.playdata.orderingservice.common.configs;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

    // 타 서버로 요청을 보낼 수 있게 해주는 객체
    @Bean
    // 유레카에 등록된 서비스명으로 요청할 수 있게 해주는 아노테이션
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
