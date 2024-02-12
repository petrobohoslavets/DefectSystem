package com.softserve.dis.service;

import com.softserve.dis.DefectsSystemApplication;
import com.softserve.dis.model.Issue;
import com.softserve.dis.model.TelegramUser;
import com.softserve.dis.repository.IssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class IssueServiceImpl implements IssueService {

    private final IssueRepository repository;

    @Autowired
    public IssueServiceImpl(IssueRepository repository) {
        this.repository = repository;
    }

    @Override
    public Issue createIssue(Issue issue) {
        Issue result = repository.save(issue);
        DefectsSystemApplication.telegramBot.setAllIssues();
        return result;
    }

    @Override
    public Optional<Issue> getIssueById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Issue> getIssuesByUserId(Long id) {
        return repository.findByUserId(id);
    }

    @Override
    public Issue setDefaultUser(Long id) {
        Optional<Issue> optionalIssue = getIssueById(id);
        Issue issue = optionalIssue.orElseThrow(() ->
                new IllegalStateException("Issue with id " + id + " does not exist"));
        issue.setTelegramUser(null);
        return repository.save(issue);
    }

    @Override
    public List<Issue> getAllIssues() {
        return repository.findAll();
    }

    @Override
    public Issue updateIssue(Issue issue) {
        Issue result = repository.save(issue);
        DefectsSystemApplication.telegramBot.setAllIssues();
        return result;
    }

    @Override
    public void deleteIssue(Long id) {
        repository.deleteById(id);
        DefectsSystemApplication.telegramBot.removeIssue(id);
    }


}
