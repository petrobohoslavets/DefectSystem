package com.softserve.dis.repository;

import com.softserve.dis.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    @Query(nativeQuery = true, value = "SELECT i FROM issues i WHERE i.employee_id = :id")
    List<Issue> findByUserId(@Param("id") Long chatId);
}
