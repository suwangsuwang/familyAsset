package org.swan.familyasset.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.swan.familyasset.Entity.Transaction;

import java.util.List;

@Mapper
public interface TransactionMapper {

    @Insert("INSERT INTO transaction(user_id,asset_id, type, price, quantity) VALUES(#{userId},#{assetId}, #{type}, #{price}, #{quantity})")
    void insert(Transaction transaction);

    @Select("SELECT * FROM transaction WHERE asset_id = #{assetId}")
    List<Transaction> findByAssetId(Long assetId);



//    @Select("SELECT * FROM transaction WHERE asset_id = #{assetId}")
//    List<Transaction> findByAssetId(Long assetId);
}
