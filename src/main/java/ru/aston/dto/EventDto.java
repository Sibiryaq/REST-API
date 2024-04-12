package ru.aston.dto;

import lombok.*;

@NoArgsConstructor
@Builder
@Setter
@Getter
@AllArgsConstructor
@ToString
public class EventDto {
    private Long id;

    private String title;

    private String description;
}
