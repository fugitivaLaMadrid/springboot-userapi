package com.fugitivalamadrid.api.userapi.repository;

import com.fugitivalamadrid.api.userapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
 //because we extends JpaRepository we can get, update,find, save, delete all users
}
