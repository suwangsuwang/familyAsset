package org.swan.familyasset.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Transaction {
    private Long id;

    private Long userId;
    private Long assetId;
    private String type;
    private BigDecimal price;
    private BigDecimal quantity;
}
