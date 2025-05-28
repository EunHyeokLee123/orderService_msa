package com.playdata.orderingservice.ordering.service;

import com.playdata.orderingservice.ordering.dto.OrderNotificationEvent;
import com.playdata.orderingservice.ordering.entity.Ordering;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

    /*
    역할: "주문 정보를 받아서 RabbitMQ로 알림 메시지를 보내는 택배 기사"

    [OrderingService] → [OrderNotificationService] → [RabbitMQ] → [관리자]
       (주문 완료!)           ("알림 보내드릴게요!")        (메시지 전달)   (알림 받음!)
     */

    // 우리가 만든 exchange, queue로 메시지를 보낼 수 있게 해줌.
    private final RabbitTemplate rabbitTemplate;

    public void sendNewOrderNotification(Ordering ordering) {
        // 주문 완료된 것만 알림 발송
        try {
            if (ordering.getOrderStatus().toString().equals("ORDERED")){
                // 알림 전용 DTO 생성
                OrderNotificationEvent event = OrderNotificationEvent.fromOrdering(ordering);

                // RabbitMq로 메시지 발송
                rabbitTemplate.convertAndSend(
                        "order.exchange", // RabbitMQConfig에서 만든 Exchange
                        "order.create",     //  Routing Key (어느 큐로 보낼지 결정)
                        event           // 보낼 데이터 (JSON으로 자동 변환됨)
                );

                log.info("Order Notification sent to admin: orderId: {}, customer: {}"
                        , ordering.getId(), ordering.getUserEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send order notification admin: orderId: {}, customer: {}"
                    , ordering.getId(), ordering.getUserEmail(), e);
            // 알림 발송을 실패해도 주문 처리는 계속 진행
            // 알림은 부가기능이니까 알림 발송을 실패해도 주문은 성공해야 함.
        }
    }
}
