package org.swan.familyasset.Consumer;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OutboxTestConsumer {

    @RabbitListener(queues = "outbox.test.queue")
    public void receive(String message) {

        System.out.println("=================================");
        System.out.println("RabbitMQ 收到消息：");
        System.out.println(message);
        System.out.println("=================================");
    }
}
