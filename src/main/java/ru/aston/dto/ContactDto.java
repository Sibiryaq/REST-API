package ru.aston.dto;

import lombok.*;

@NoArgsConstructor
@Builder
@Setter
@Getter
@AllArgsConstructor
@ToString
public class ContactDto {
    private Long id;

    private String phone;

    private String address;
}
