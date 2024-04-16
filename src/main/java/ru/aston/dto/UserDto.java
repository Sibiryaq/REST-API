package ru.aston.dto;

import lombok.*;

@NoArgsConstructor
@Builder
@Setter
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserDto {

    private Long id;

    private String name;

    private String email;

    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
