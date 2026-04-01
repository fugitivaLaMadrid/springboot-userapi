package com.fugitivalamadrid.api.userapi.repository;

import com.fugitivalamadrid.api.userapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
 //because we extends JpaRepository we can get, update,find, save, delete all users
    List<User> findByUsernameContainingIgnoreCase(String username);
}
