package com.playdata.orderingservice.ordering.controller;

import com.playdata.orderingservice.common.auth.TokenUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class SseController {

    @GetMapping("/subscribe")
    public void subscribe(@AuthenticationPrincipal TokenUserInfo userInfo) {
        SseEmitter emitter = new SseEmitter(24 * 60 * 60 * 1000L);
        log.info("Subscribing to {}", userInfo.getEmail());

        // 연결 성공 메시지 전송
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connect")
                            .data("connected")
            );

            // 30초(임의의 시간)마다 heartbeat 메시지를 전송해서 연결 유지
            // 클라이언트에서 사용하는 EventSourcePolyfill은 45초동안 활동이 없으면 자기맘대로 연결 종료
            // 이를 방지하기 위해서, 45초보다 짧은 텀으로 message를 보내서 활동을 만듬.
            Executors.newScheduledThreadPool(1)
                    .scheduleAtFixedRate(() -> {
                        // 일정하게 동작시킬 로직을 작성
                        try {
                            emitter.send(
                                    SseEmitter.event()
                                            .name("heartbeat")
                                            // 클라이언트 단이 살아있는 지 확인하는 메시지
                                            .data("keep-alive")
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                            log.info("Fail to send heartbeat");
                        }
                    }, 30, 30, TimeUnit.SECONDS);

        } catch (IOException e) {
            e.printStackTrace();
            log.info("Fail to connect");
        }
    }

}
