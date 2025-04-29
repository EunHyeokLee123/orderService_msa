package com.playdata.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory {


    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain)->{
            // exchange: 현재 요청과 응답에 대한 정보를 담은 객체
            // chain: 게이트웨이 필터 체인 -> 필터 통과 여부를 결정
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custome Filter active! request id = {}", request.getId());
            log.info("Request URI = {}", request.getURI());

            return chain.filter(exchange).then(Mono.fromRunnable(()->{
                //  then 메소드 내부에 필터 체인 처리 완료 후 실행할 post-filter 로직 정의 가능
                log.info("Custome Post Filter active!");
                log.info("response code: {}", response.getStatusCode());

            }));
        };
    }
}
