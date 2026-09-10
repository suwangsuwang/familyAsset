package org.swan.familyasset.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swan.familyasset.Entity.User;
import org.swan.familyasset.Service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        userService.register(user);
        return "注册成功";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        String token = userService.login(user);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return result;
    }
}
