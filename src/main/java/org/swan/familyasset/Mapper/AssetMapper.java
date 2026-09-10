package org.swan.familyasset.Mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.swan.familyasset.Entity.Asset;

import java.util.List;

@Mapper
public interface AssetMapper {

    @Insert("INSERT INTO asset(user_id, symbol, name) VALUES(#{userId}, #{symbol}, #{name})")
    void insert(Asset asset);

    @Select("SELECT * FROM asset WHERE user_id = #{userId}")
    List<Asset> findByUserId(Long userId);

    @Select("SELECT * FROM asset WHERE id = #{id}")
    Asset findById(Long id);
}
