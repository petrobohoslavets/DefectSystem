package com.softserve.dis.service;

import com.softserve.dis.DefectsSystemApplication;
import com.softserve.dis.bot.TelegramBot;
import com.softserve.dis.model.TelegramUser;
import com.softserve.dis.repository.TelegramUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class TelegramUserServiceImpl implements TelegramUserService {

    private final TelegramUserRepository repository;

    @Autowired
    public TelegramUserServiceImpl(TelegramUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public TelegramUser createTelegramUser(TelegramUser user) {
        return repository.save(user);
    }

    @Override
    public Optional<TelegramUser> getTelegramUserById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<TelegramUser> getAllTelegramUsers() {
        return repository.findAll();
    }

    @Override
    public TelegramUser updateTelegramUser(TelegramUser user) {
        DefectsSystemApplication.telegramBot.updateUser(user);
        return repository.save(user);
    }

    @Override
    public void deleteTelegramUser(Long id) {
        repository.deleteById(id);
        DefectsSystemApplication.telegramBot.deleteUser(id);
    }

    @Override
    public TelegramUser approveRegistration(Long id, String role) {
        Optional<TelegramUser> optionalEmployee = repository.findById(id);
        TelegramUser employee = optionalEmployee.orElseThrow(() ->
                new IllegalStateException("User with id " + id + " does not exist"));

        employee.setRole(role);
        employee.setActivated(true);
        DefectsSystemApplication.telegramBot.approveRegistration(id, role);

        return repository.save(employee);
    }
}
