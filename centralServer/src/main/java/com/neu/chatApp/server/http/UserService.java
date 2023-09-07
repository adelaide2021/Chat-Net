package com.neu.chatApp.server.http;

import com.neu.chatApp.client.peerToPeer.data.ClientData;
import com.neu.chatApp.model.user.User;
import com.neu.chatApp.model.node.Node;
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



@Slf4j
@Service
public class UserService {

    private final UserRepository  UserRepository ;

    @Autowired
    public UserService(UserRepository  UserRepository  ) {
        this.UserRepository  = UserRepository  ;
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

        // accept the registry
        // generate salt for the user
        String salt = Encryption.saltGenerator();
        // encrypt the password
        String encryptedPassword = Encryption.md5(password, salt);
        // create an account
        User user = new User(username, encryptedPassword, salt);
        // save the entity
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
        // abort the request
        if (user.isOnline()) {
            response.put("error", "Your account has logged in at somewhere");
            return ResponseEntity.badRequest().body(response);
        }

        // record the ip and port of this time login
        // db
        UserRepository.updateLogin(user.getId(), true);
        UserRepository.updateHostnameAndPort(id, hostname, port);

        // return the leader hostname and port of the p to p network
        // and the id the user
        response.put("id", id);
        response.put("nickname", user.getUsername());
        // if empty list, then the node will be assigned as leader
        if (ClientData.clientLiveNodes.size() == 0) {
            // given the server hostname and port
            try {
                response.put("hostname", InetAddress.getLocalHost().getHostAddress());
                response.put("port", ClientData.myPort);
            } catch (UnknownHostException ignored) {}
        } else {
            // return the leader hostname and port
            Node leaderNode = ClientData.clientLiveNodes.getLeaderNode();
            response.put("hostname", leaderNode.getHostname());
            response.put("port", leaderNode.getPort());
        }
        return ResponseEntity.ok(response);
    }

    @Transactional
    public void logout(Long userId) {
        if (ClientData.clientLiveNodes.isContain(userId)) {
            ClientData.clientLiveNodes.remove(userId);
            log.info("A node left id: " + userId);
        }
        UserRepository  .updateLogin(userId, false);
    }
}


