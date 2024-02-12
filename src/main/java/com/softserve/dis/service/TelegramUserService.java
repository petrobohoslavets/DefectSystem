package com.softserve.dis.service;

import com.softserve.dis.model.TelegramUser;

import java.util.List;
import java.util.Optional;

public interface TelegramUserService {
    TelegramUser createTelegramUser(TelegramUser user);
    Optional<TelegramUser> getTelegramUserById(Long id);
    List<TelegramUser> getAllTelegramUsers();
    TelegramUser updateTelegramUser(TelegramUser user);
    void deleteTelegramUser(Long id);
    TelegramUser approveRegistration(Long id, String role);
}
