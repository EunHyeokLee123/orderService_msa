package com.playdata.gatewayservice.filter;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GlobalFilter
    // 제네릭에도 우리가 선언한 정적 클래스를 넣으면
        extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    // Filter 객체가 생성되서 Bean으로 등록 될때, 부모 클래스의 생성자에게
    // 이미 정적(static)으로 세팅된 특정 설정값을 전달합니다.
    public GlobalFilter() {

        super(Config.class);

    }

    @Override
    // override하는 메소드의 매개변수에 우리가 선언한 정적 클래스를 사용할 수 있음.
    public GatewayFilter apply(Config config) {
        return null;
    }

    @Getter @Setter
    @ToString @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        // 필터 동작을 동적으로 변경하거나 설정하기 위해 사용함 (선택사항)
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;

    }
}
