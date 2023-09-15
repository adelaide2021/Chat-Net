package com.neu.chatApp.centralServer.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles basic login and registration operations with server-side database.
 * Called on each client's HTTP request.
 */

@CrossOrigin
@RestController
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
     *  User for leader node notify the server which node has exited.
     *
     *  @param userId the user id
     */
    @PostMapping("/logout")
    public void logout(@RequestBody Long userId) {
        userService.logout(userId);
    }
}
