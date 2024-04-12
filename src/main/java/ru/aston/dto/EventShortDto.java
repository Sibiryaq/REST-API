package ru.aston.dto;

import lombok.*;

@NoArgsConstructor
@Builder
@Setter
@Getter
@AllArgsConstructor
@ToString
public class EventShortDto {

    protected Long id;

    private String title;

    private String description;

    private UserDto initiator;

}
