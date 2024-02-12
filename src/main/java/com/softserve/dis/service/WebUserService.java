package com.softserve.dis.service;

import com.softserve.dis.model.WebUser;

import java.util.List;
import java.util.Optional;

public interface WebUserService {
    WebUser createWebUser(WebUser user);
    Optional<WebUser> getWebUserById(Long id);
    List<WebUser> getAllWebUsers();
    WebUser updateWebUser(WebUser user);
    void deleteWebUser(Long id);
    WebUser authenticateUser(String login, String password);

    List<Object[]> findUsersFromBothTables();
    public WebUser approveRegistration(Long id, String role);
}
