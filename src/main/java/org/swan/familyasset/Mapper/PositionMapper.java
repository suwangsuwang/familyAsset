package org.swan.familyasset.Mapper;

import org.apache.ibatis.annotations.*;
import org.swan.familyasset.Entity.Position;

import java.util.List;

@Mapper
public interface PositionMapper {


    @Insert("""
            INSERT INTO `position`(user_id, asset_id, quantity, avg_cost)
            VALUES(#{userId}, #{assetId}, #{quantity}, #{avgCost})
            """)
    void insert(Position position);

    @Select("""
            SELECT * FROM `position`
            WHERE user_id = #{userId}
            AND asset_id = #{assetId}
            """)
    Position findByUserIdAndAssetId(Long userId, Long assetId);

    @Update("""
            UPDATE `position`
            SET quantity = #{quantity},
                avg_cost = #{avgCost}
            WHERE id = #{id}
            """)
    void update(Position position);

    @Select("""
            SELECT * FROM position
            WHERE user_id = #{userId}
            """)
    List<Position> findByUserId(Long userId);

    @Delete("""
            DELETE FROM `position`
            WHERE id = #{id}
            """)
    void deleteById(Long id);

}
