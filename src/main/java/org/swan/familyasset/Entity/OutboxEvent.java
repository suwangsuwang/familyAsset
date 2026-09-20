package org.swan.familyasset.Entity;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutboxEvent {

    private Long id;


    private String eventType;


    private Long aggregateId;


    private  String payload;


    private String status;


    private LocalDateTime createdAt;
}
