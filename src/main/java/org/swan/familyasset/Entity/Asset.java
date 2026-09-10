package org.swan.familyasset.Entity;

import lombok.Data;

@Data
public class Asset {
    private Long id;
    private Long userId;
    private String symbol;
    private String name;
}
