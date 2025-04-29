package com.playdata.userservice.common.configs;

// CORS(Cross-Origin Resource Sharing) -> 교차 출처 자원 공유
// CORS: 웹 어플리케이션이 다른 도메인에서 리소스를 요청할 때 발생하는 보안 문제를 해결하기 위해 사용

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**") // URL 경로 설정
                // 우리 리엑트(우리 url을 입력)의 요청만 받겠다
                .allowedOrigins("http://localhost:5173") // 오리진 설정
                // 어떤 메소드든 요청을 다 받겠다.
                .allowedMethods("*") // 요청 방식 허용
                .allowedHeaders("*") // Header 정보 허용 여부
                .allowCredentials(true); // 인증 정보를 포함한 요청을 허용할 것인가
    }
}
