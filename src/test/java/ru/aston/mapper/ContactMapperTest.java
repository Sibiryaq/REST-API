package ru.aston.mapper;

import ru.aston.dto.ContactDto;
import ru.aston.model.Contact;
import org.junit.jupiter.api.Test;
import ru.aston.testData.TestConstants;
import ru.aston.testUtil.TestGetProvider;

import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static org.testcontainers.shaded.org.hamcrest.Matchers.equalTo;

class ContactMapperTest {

    @Test
    void toDto() {
        Contact contact = TestGetProvider.getContact(TestConstants.CONTACT_PHONE, TestConstants.CONTACT_ADDRESS, TestConstants.FIRST_ID);

        ContactDto actual = ContactMapper.toDto(contact);

        assertThat(actual.getPhone(), equalTo(contact.getPhone()));
        assertThat(actual.getAddress(), equalTo(contact.getAddress()));
    }

    @Test
    void toEntity() {
        ContactDto contactDto = ContactDto.builder()
                .id(TestConstants.FIRST_ID)
                .phone(TestConstants.CONTACT_PHONE)
                .address(TestConstants.CONTACT_ADDRESS)
                .build();

        Contact actual = ContactMapper.toEntity(contactDto, TestConstants.FIRST_ID);

        assertThat(actual.getPhone(), equalTo(contactDto.getPhone()));
        assertThat(actual.getAddress(), equalTo(contactDto.getAddress()));
        assertThat(actual.getEventId(), equalTo(TestConstants.FIRST_ID));
    }
}