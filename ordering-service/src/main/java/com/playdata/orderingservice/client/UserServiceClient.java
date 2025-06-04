package com.playdata.orderingservice.client;

import com.playdata.orderingservice.common.dto.CommonResDTO;
import com.playdata.orderingservice.ordering.dto.UserResDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 호출하고자 하는 서비스의 Eureka 등록명을 작성하면 됨.
@FeignClient(name = "user-service", url = "http://user-service.default.svc.cluster.local:8081")
public interface UserServiceClient {

    // 인터페이스에는 요청 방식, 요청 url, 전달하고자 하는 데이터,
    // 응답 받고자하는 데이터의 형태를 추상메서드 형식으로 선언함.
    // 그럼 OpenFeign에서 우리가 작성한 인터페이스의 구현체를 알아서 만들어 줌.

    @GetMapping("/user/findByEmail")
    CommonResDTO<UserResDTO> findByEmail(@RequestParam String email);



}
