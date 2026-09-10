package org.swan.familyasset.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitRecord {

    private Long id;

    private Long userId;

    private Long assetId;

    // 当前持仓数量
    private BigDecimal quantity;

    // 本次卖出的成本
    private BigDecimal cost;

    // 本次卖出的收入
    private BigDecimal revenue;

    // 本次实现收益
    private BigDecimal profit;


}
