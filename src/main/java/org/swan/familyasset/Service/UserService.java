package org.swan.familyasset.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swan.familyasset.Entity.User;
import org.swan.familyasset.Mapper.UserMapper;
import org.swan.familyasset.Utils.JwtUtil;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public void register(User user) {

        // 1. 判断用户是否存在
        int existCount = userMapper.countByUsername(user.getUsername());
        if (existCount > 0) {
            throw new RuntimeException("用户已存在");
        }

        // 2. 保存用户
        userMapper.insert(user);
    }

    public String login(User user) {

        User dbUser = userMapper.findByUsername(user.getUsername());

        if (dbUser == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!dbUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 简单 token (先这样, 后面升级)
        return JwtUtil.generateToken(dbUser.getId());
    }
}
