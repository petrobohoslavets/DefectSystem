package com.softserve.dis.repository;

import com.softserve.dis.model.WebUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WebUserRepository extends JpaRepository<WebUser, Long> {
    Optional<WebUser> findByLogin(String login);

    @Query(value = "SELECT t.id, t.first_name AS 'name', t.last_name AS 'surname', t.role, t.activated, 'telegramUsers' FROM telegram_user t " +
            "UNION " +
            "SELECT w.id, w.first_name AS 'name', w.last_name AS 'surname', w.role, w.activated, 'webUsers' FROM Web_User w", nativeQuery = true)
    List<Object[]> findUsersFromBothTables();
}