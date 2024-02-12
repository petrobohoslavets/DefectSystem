package com.softserve.dis.controller;

import com.softserve.dis.model.TelegramUser;
import com.softserve.dis.service.TelegramUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/telegramUsers")
public class TelegramUserController {

    private TelegramUserService telegramUserService;

    @Autowired
    public TelegramUserController(TelegramUserService telegramUserService) {
        this.telegramUserService = telegramUserService;
    }

    @PostMapping
    public TelegramUser createTelegramUser(@RequestBody TelegramUser user) {
        return telegramUserService.createTelegramUser(user);
    }

    @GetMapping("/{id}")
    public TelegramUser getTelegramUserById(@PathVariable Long id) {
        return telegramUserService.getTelegramUserById(id).orElse(null);
    }

    @GetMapping
    public List<TelegramUser> getAllTelegramUsers() {
        return telegramUserService.getAllTelegramUsers();
    }

    @PutMapping
    public TelegramUser updateTelegramUser(@RequestBody TelegramUser user) {
        return telegramUserService.updateTelegramUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteTelegramUser(@PathVariable Long id) {
        telegramUserService.deleteTelegramUser(id);
    }

    @PutMapping("/approve/{id}/{role}")
    public TelegramUser approveRegistration(@PathVariable Long id, @PathVariable String role) {
        return telegramUserService.approveRegistration(id, role);
    }
}
