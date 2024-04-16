package ru.aston.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Contact {

    private Long id;

    private String phone;

    private String address;

    private Long eventId;
}
