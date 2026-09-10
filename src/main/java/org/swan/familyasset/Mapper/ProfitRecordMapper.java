package org.swan.familyasset.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.swan.familyasset.Entity.ProfitRecord;

@Mapper
public interface ProfitRecordMapper {

    @Insert("""
            INSERT INTO profit_record(
                user_id,
                asset_id,
                quantity,
                cost,
                revenue,
                profit
            )
            VALUES(
                #{userId}
                #{assetId}
                #{quantity}
                #{cost}
                #{revenue}
                #{profit}
            )
            """)
    int insert(ProfitRecord record);
}
