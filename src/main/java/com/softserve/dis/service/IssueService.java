package com.softserve.dis.service;

import com.softserve.dis.model.Issue;

import java.util.List;
import java.util.Optional;

public interface IssueService {
    Issue createIssue(Issue issue);
    Optional<Issue> getIssueById(Long id);
    List<Issue> getAllIssues();
    Issue updateIssue(Issue issue);
    void deleteIssue(Long id);

    List<Issue> getIssuesByUserId(Long id);

    Issue setDefaultUser(Long id);
}
