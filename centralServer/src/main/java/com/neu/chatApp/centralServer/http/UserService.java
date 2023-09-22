package com.neu.chatApp.centralServer.http;

import com.neu.chatApp.centralServer.peerToPeer.data.ServerData;
import com.neu.chatApp.common.model.node.Node;
import com.neu.chatApp.centralServer.db.User;
import com.neu.chatApp.util.Encryption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


// 1、用server data判断leader node是否已经存在，问题是在login的时候并没有同步更新server data中的数据？？？
// logout的时候remove这个node，但从哪里加入的呢？？？
@Slf4j
@Service
public class UserService {

    private final UserRepository UserRepository ;

    @Autowired
    public UserService(UserRepository  UserRepository) {

        this.UserRepository = UserRepository;
    }

    @Transactional
    public ResponseEntity<String> signup(Map<String, String> data) {
        // parse the map
        String username = data.get("username");
        String password = data.get("password");

        // look up for the user if it is already registered
        Long id = UserRepository.getUserByName(username);
        if (id != null) {
            return ResponseEntity.badRequest().body("The user has been registered");
        }

        String salt = Encryption.saltGenerator();
        String encryptedPassword = Encryption.md5(password, salt);
        User user = new User(username, encryptedPassword, salt);
        UserRepository.save(user);
        return ResponseEntity.ok("Welcome to join us");
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> login(Map<String, Object> data) {
        // parse map
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        String hostname = (String) data.get("hostname");
        int port = (int) data.get("port");

        Map<String, Object> response = new HashMap<>();
        // check if the user exists
        Long id = UserRepository.getUserByName(username);
        if (id == null) {
            response.put("error", "Account doesn't exist");
            return ResponseEntity.badRequest().body(response);
        }
        // check password
        User user = (User) UserRepository.findById(id).get();
        String salt = user.getSalt();
        String givenPassword = Encryption.md5(password, salt);
        String correctPassword = user.getPassword();
        // incorrect password
        if (!correctPassword.equals(givenPassword)) {
            response.put("error", "Incorrect password");
            return ResponseEntity.badRequest().body(response);
        }
        // check if the user has already login somewhere
        if (user.isOnline()) {
            response.put("error", "Your account has logged in at somewhere");
            return ResponseEntity.badRequest().body(response);
        }

        // record the hostname and port used during this login in the database
        UserRepository.updateHostnameAndPort(id, hostname, port);
        UserRepository.updateLogin(user.getId(), true);

        // return the id and the username of the user
        response.put("id", id);
        response.put("username", user.getUsername());

        // return the leader hostname and port of the p to p network
        // if empty list, then the node will be assigned as leader
        if (ServerData.serverLiveNodes.size() == 0) {
            // given the server hostname and port
            try {
                response.put("hostname", InetAddress.getLocalHost().getHostAddress());
                response.put("port", ServerData.mySocketPort);
            } catch (UnknownHostException ignored) {}
        } else {
            // return the leader hostname and port
            Node leaderNode = ServerData.serverLiveNodes.getLeaderNode();
            response.put("hostname", leaderNode.getHostname());
            response.put("port", leaderNode.getPort());
        }
        return ResponseEntity.ok(response);
    }

    @Transactional
    public void logout(Long userId) {
        if (ServerData.serverLiveNodes.isContain(userId)) {
            ServerData.serverLiveNodes.remove(userId);
            log.info("A node left id: " + userId);
        }
        UserRepository.updateLogin(userId, false);
    }
}


