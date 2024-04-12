package ru.aston.dto;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@ToString
public class EventResponseDto extends EventShortDto {

    @ToString.Exclude
    private ContactDto contacts;

    @ToString.Exclude
    private Set<UserDto> participants = new HashSet<>();
}
