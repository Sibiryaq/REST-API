package ru.aston.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class User {

    private Long id;

    private String name;

    private String email;
}
