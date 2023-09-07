package com.neu.chatApp.server.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/*
user 这部分就是负责最基础的登陆注册操作，同时实现方式也是最基础的server端连接数据库
每次client发送http请求的时候都hit了这个API而已
 */
@CrossOrigin
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Signup.
     *
     * @param data take nickname, password
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String, String> data) {
        return userService.signup(data);
    }

    /**
     * Login.
     *
     * @param data take email, password, hostname, port
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> data) {
        return userService.login(data);
    }

/**
 *  * User for leader node notify the server which node has exited.
 *      *
 *      * @param userId the user id
 *      */

    @PostMapping("/logout")
    public void logout(@RequestBody Long userId) {
        userService.logout(userId);
    }

}
