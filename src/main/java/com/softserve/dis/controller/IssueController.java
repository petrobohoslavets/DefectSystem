package com.softserve.dis.controller;

import com.softserve.dis.model.Issue;
import com.softserve.dis.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/issues")
public class IssueController {

    private final IssueService issueService;
    @Autowired
    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public Issue createIssue(@RequestBody Issue issue) {
        return issueService.createIssue(issue);
    }

    @GetMapping("/{id}")
    public Issue getIssueById(@PathVariable Long id) {
        return issueService.getIssueById(id).orElse(null);
    }

    @GetMapping("/user={id}")
    public List<Issue> getIssueByUserId(@PathVariable Long id) {
        return issueService.getIssuesByUserId(id);
    }

    @GetMapping
    public List<Issue> getAllIssues() {
        return issueService.getAllIssues();
    }

    @PutMapping
    public Issue updateIssue(@RequestBody Issue issue) {
        return issueService.updateIssue(issue);
    }

    @PutMapping("/default={id}")
    public Issue setDefaultUser(@PathVariable Long id) {return issueService.setDefaultUser(id);}

    @DeleteMapping("/{id}")
    public void deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
    }
}
