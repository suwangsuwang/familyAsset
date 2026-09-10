package org.swan.familyasset.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Position {

    private Long id;

    private Long userId;

    private Long assetId;

    // 当前持仓数量
    private BigDecimal quantity;

    // 评价成本
    private  BigDecimal avgCost;
}
