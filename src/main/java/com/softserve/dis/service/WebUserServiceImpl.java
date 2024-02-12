package com.softserve.dis.service;

import com.softserve.dis.model.WebUser;
import com.softserve.dis.repository.WebUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class WebUserServiceImpl implements WebUserService {

    @Autowired
    private WebUserRepository webUserRepository;

    @Override
    public WebUser createWebUser(WebUser user) {
        return webUserRepository.save(user);
    }


    @Override
    public Optional<WebUser> getWebUserById(Long id) {
        return webUserRepository.findById(id);
    }

    @Override
    public List<WebUser> getAllWebUsers() {
        return webUserRepository.findAll();
    }

    @Override
    public WebUser updateWebUser(WebUser user) {
        return webUserRepository.save(user);
    }

    @Override
    public void deleteWebUser(Long id) {
        webUserRepository.deleteById(id);
    }

    public WebUser authenticateUser(String login, String password) {
        Optional<WebUser> userOpt = webUserRepository.findByLogin(login);
        if (userOpt.isPresent()) {
            WebUser user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return new WebUser();
    }
    public List<Object[]> findUsersFromBothTables() {
        return webUserRepository.findUsersFromBothTables();
    }
    public WebUser approveRegistration(Long id, String role) {
        Optional<WebUser> optionalWebUser = webUserRepository.findById(id);
        WebUser user = optionalWebUser.orElseThrow(() -> new IllegalStateException("User with id " + id + " does not exist"));
        user.setRole(role);
        user.setActivated(true);
        return webUserRepository.save(user);
    }
}
