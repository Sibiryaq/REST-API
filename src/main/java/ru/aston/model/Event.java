package ru.aston.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Event {
    private Long id;

    private String title;

    private String description;

    private User initiator;

    private Contact contact;

    private Set<User> participants = new HashSet<>();

    public Long getInitiatorId() {
        if (initiator == null) return 0L;
        return initiator.getId();
    }

    public Long getContactId() {
        if (contact == null) return 0L;
        return contact.getId();
    }
}
