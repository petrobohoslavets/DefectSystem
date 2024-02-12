package com.softserve.dis.controller;

import com.softserve.dis.model.WebUser;
import com.softserve.dis.service.WebUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/webUsers")
public class WebUserController {

    private final WebUserService webUserService;

    @Autowired
    public WebUserController(WebUserService webUserService) {
        this.webUserService = webUserService;
    }


    @PostMapping
    public WebUser createWebUser(@RequestBody WebUser user) {
        return webUserService.createWebUser(user);
    }

    @PostMapping("/upload")
    public WebUser registerWebUser(@RequestParam("firstName") String firstName,
                                   @RequestParam("lastName") String lastName,
                                   @RequestParam("Username") String Username,
                                   @RequestParam("password") String password,
                                   @RequestParam("photo") MultipartFile photo) {
        WebUser user = new WebUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setLogin(Username);
        user.setPassword(password);
        if(photo.isEmpty()) {
            user.setPhotoPath(null);
        } else {
            try {
                byte[] bytes = photo.getBytes();
                Path path = Paths.get("src/main/resources/static/images/" + photo.getOriginalFilename());
                Files.write(path, bytes);
                user.setPhotoPath(path.toString());
            } catch (IOException e) {
                e.printStackTrace();
                user.setPhotoPath(null);
            }
        }
        return webUserService.createWebUser(user);
    }

    @GetMapping("/{id}")
    public WebUser getWebUserById(@PathVariable Long id) {
        return webUserService.getWebUserById(id).orElse(null);
    }

    @GetMapping
    public List<WebUser> getAllWebUsers() {
        return webUserService.getAllWebUsers();
    }

    @PutMapping
    public WebUser updateWebUser(@RequestBody WebUser user) {
        return webUserService.updateWebUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteWebUser(@PathVariable Long id) {
        webUserService.deleteWebUser(id);
    }

    @GetMapping("/login")
    public WebUser login(@RequestParam("loginUsername") String login, @RequestParam("loginPassword") String password) {
        return webUserService.authenticateUser(login, password);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Object[]>> getAllUsers() {
        List<Object[]> users = webUserService.findUsersFromBothTables();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/approve/{id}/{role}")
    public WebUser approveRegistration(@PathVariable Long id, @PathVariable String role) {
        return webUserService.approveRegistration(id, role);
    }
}