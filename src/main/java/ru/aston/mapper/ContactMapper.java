package ru.aston.mapper;

import ru.aston.dto.ContactDto;
import ru.aston.model.Contact;

public class ContactMapper {

    private ContactMapper() {
    }

    public static ContactDto toDto(Contact contact) {
        if (contact == null) return null;

        return ContactDto.builder()
                .id(contact.getId())
                .phone(contact.getPhone())
                .address(contact.getAddress())
                .build();
    }

    public static Contact toEntity(ContactDto contactDto, Long eventId) {
        if (contactDto == null) return null;

        Contact contact = new Contact();
        contact.setId(contactDto.getId());
        contact.setPhone(contactDto.getPhone());
        contact.setAddress(contactDto.getAddress());
        contact.setEventId(eventId);

        return contact;
    }
}
