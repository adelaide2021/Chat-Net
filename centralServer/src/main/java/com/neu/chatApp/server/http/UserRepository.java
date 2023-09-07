package com.neu.chatApp.server.http;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository{
    @Query(value = "SELECT id FROM user_info WHERE usernane = :username", nativeQuery = true)
    Long getUserByName(@Param("username") String email);

    @Modifying
    @Query(value = "UPDATE user_info SET hostname = :userHostname, port = :userPort WHERE id = :userId", nativeQuery = true)
    void updateHostnameAndPort(@Param("userId") Long id, @Param("userHostname") String hostname, @Param("userPort") int port);


    @Modifying
    @Query(value = "UPDATE user_info SET is_login = :userLogin WHERE id = :userId", nativeQuery = true)
    void updateLogin(@Param("userId") Long id, @Param("userLogin") boolean isLogin);
}
