package org.swan.familyasset.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @PostConstruct
    public void init() {
        System.setProperty("spring.amqp.deserialization.trust.all", "true");
    }

    public static final String SELL_EXCHANGE = "sell.exchange";
    public static final String PROFIT_QUEUE = "profit.queue";
    public static final String SELL_SUCCESS_ROUTING_KEY = "sell.success";

    @Bean
    public DirectExchange sellExchange() {
        return new DirectExchange(SELL_EXCHANGE);
    }

    @Bean
    public Queue profitQueue() {
        return new Queue(PROFIT_QUEUE);
    }

    @Bean
    public Binding profitBinding() {
        return BindingBuilder.bind(profitQueue())
                .to(sellExchange())
                .with(SELL_SUCCESS_ROUTING_KEY);
    }
}
