package org.swan.familyasset;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("org.swan.familyasset.Mapper")
@EnableScheduling
public class FamilyAssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyAssetApplication.class, args);
    }

}
