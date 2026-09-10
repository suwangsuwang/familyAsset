package org.swan.familyasset.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SellEvent implements Serializable {

    private Long userId;

    private Long assetId;

    private BigDecimal quantity;

    private BigDecimal sellPrice;

    private BigDecimal avgCost;
}
