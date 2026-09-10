package org.swan.familyasset.VO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionVO {

    private String assetName;

    private BigDecimal quantity;

    private BigDecimal avgCost;

    private BigDecimal marketPrice;

    private BigDecimal profit;
}
