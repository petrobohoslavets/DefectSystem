package com.softserve.dis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String room;
    private String description;
    private String photo;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private TelegramUser telegramUser;

    public Issue(TelegramUser telegramUser, String room, String description, String photoPath) {
        this.telegramUser = telegramUser;
        this.room = room;
        this.description = description;
        this.photo = photoPath;
    }
}
