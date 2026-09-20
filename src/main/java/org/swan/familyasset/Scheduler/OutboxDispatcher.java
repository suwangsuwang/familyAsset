package org.swan.familyasset.Scheduler;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swan.familyasset.Entity.OutboxEvent;
import org.swan.familyasset.Mapper.OutboxEventMapper;

import java.util.List;

@Component
public class OutboxDispatcher {

    private final OutboxEventMapper outboxEventMapper;

    private final RabbitTemplate rabbitTemplate;

    public OutboxDispatcher(OutboxEventMapper outboxEventMapper, RabbitTemplate rabbitTemplate) {
        this.outboxEventMapper = outboxEventMapper;
        this.rabbitTemplate = rabbitTemplate;
    }


    @Scheduled(fixedDelay = 5000)
    public void dispatch() {

        System.out.println("开始扫码 Outbox...");

        List<OutboxEvent> events = outboxEventMapper.findNewEvents();

        System.out.println("发现" + events.size() + "个 NEW 事件");

        for (OutboxEvent event : events) {
            System.out.println("准备发送事件: id=" + event.getId() + ", type=" + event.getEventType());

            // 模拟发送
//            System.out.println("模拟 RabbitMQ 发送成功");

//            System.out.println(
//                    "处理事件:" +
//                            "id=" + event.getId() +
//                            ", type=" + event.getEventType() +
//                            ", payload=" + event.getPayload()
//
//            );

            rabbitTemplate.convertAndSend(
                    "outbox.test.exchange",
                    "outbox.test",
                    event.getPayload()
            );

            System.out.println("RabbitMQ 发送成功");

            int rows = outboxEventMapper.markSent(event.getId());
//            outboxEventMapper.markSent(event.getId());
            System.out.println(
                    "事件状态更新完成:" + rows
            );
//            System.out.println(
//                    "事件处理完成, 更新行数: " + rows
//            );
        }
    }
}
