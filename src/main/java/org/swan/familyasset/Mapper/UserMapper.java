package org.swan.familyasset.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.swan.familyasset.Entity.User;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(username, password) VALUES(#{username}, #{password})")
    void insert(User user);

    @Select("SELECT COUNT(1) FROM user WHERE username = #{username}")
    int countByUsername(String username);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
}
