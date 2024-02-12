package com.softserve.dis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TelegramUser {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String role = "none";
    private boolean activated = false;

    @JsonIgnore
    @OneToMany(mappedBy = "telegramUser")
    private List<Issue> issueList;

    public TelegramUser(Long id) {
        this.id = id;
    }
}
