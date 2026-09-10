package org.swan.familyasset;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.swan.familyasset.Mapper")
public class FamilyAssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyAssetApplication.class, args);
    }

}
