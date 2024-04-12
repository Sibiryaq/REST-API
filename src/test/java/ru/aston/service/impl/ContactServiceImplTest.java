package ru.aston.service.impl;

import ru.aston.dto.ContactDto;
import ru.aston.mapper.ContactMapper;
import ru.aston.model.Contact;
import ru.aston.model.Event;
import ru.aston.model.User;
import ru.aston.repository.impl.ContactDaoImpl;
import ru.aston.util.GetProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.aston.testData.TestConstants;
import ru.aston.testUtil.TestGetProvider;

import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactDaoImpl contactDao;

    @InjectMocks
    private ContactServiceImpl contactService;

    private User initiator;
    private Event event;

    @BeforeEach
    void setUp() {
        initiator = TestGetProvider.getUser(TestConstants.FIRST_USER_NAME, TestConstants.FIRST_USER_EMAIL);
        initiator.setId(TestConstants.FIRST_ID);

        event = TestGetProvider.getEvent(TestConstants.FIRST_EVENT_TITLE, TestConstants.FIRST_EVENT_DESCRIPTION, initiator);
        event.setId(TestConstants.FIRST_ID);
    }

    @Test
    void saveContact_whenEventDoesNotContainContact_returnSavedContact() throws SQLException {
        Contact expected = TestGetProvider.getContact(TestConstants.CONTACT_PHONE, TestConstants.CONTACT_ADDRESS, event.getId());

        try (MockedStatic<GetProvider> getProvider = Mockito.mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getEvent(anyLong())).thenReturn(event);
            getProvider.when(() -> GetProvider.getUser(anyLong())).thenReturn(initiator);
            when(contactDao.save(any(Contact.class), anyLong())).thenReturn(Optional.of(expected));

            ContactDto actual = contactService.saveContact(ContactMapper.toDto(expected), TestConstants.FIRST_ID, TestConstants.FIRST_ID);

            assertThat(actual.getAddress(), equalTo(expected.getAddress()));
            verify(contactDao, times(1)).save(any(Contact.class), anyLong());
            verify(contactDao, never()).update(any(Contact.class));
        }
    }

    @Test
    void saveContact_whenEventContainContact_returnUpdatedContact() throws SQLException {
        Contact expected = TestGetProvider.getContact(TestConstants.CONTACT_PHONE, TestConstants.CONTACT_ADDRESS, event.getId());
        expected.setId(TestConstants.FIRST_ID);
        expected.setEventId(event.getId());
        event.setContact(expected);

        try (MockedStatic<GetProvider> getProvider = Mockito.mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getEvent(anyLong())).thenReturn(event);
            getProvider.when(() -> GetProvider.getUser(anyLong())).thenReturn(initiator);
            getProvider.when(() -> GetProvider.getContact(anyLong())).thenReturn(expected);
            doNothing().when(contactDao).update(any(Contact.class));

            ContactDto actual = contactService.saveContact(ContactMapper.toDto(TestGetProvider.getContact(null, TestConstants.UPDATED_CONTACT_ADDRESS, event.getId())), TestConstants.FIRST_ID, TestConstants.FIRST_ID);

            assertThat(actual.getAddress(), equalTo(TestConstants.UPDATED_CONTACT_ADDRESS));
            verify(contactDao, times(1)).update(any(Contact.class));
            verify(contactDao, never()).save(any(Contact.class), anyLong());
        }
    }

    @Test
    void deleteContact() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = Mockito.mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getEvent(anyLong())).thenReturn(event);
            getProvider.when(() -> GetProvider.getUser(anyLong())).thenReturn(initiator);
            doNothing().when(contactDao).deleteById(anyLong());

            Long deletedContactId = contactService.deleteContact(TestConstants.FIRST_ID, TestConstants.FIRST_ID, TestConstants.FIRST_ID);

            assertThat(deletedContactId, equalTo(TestConstants.FIRST_ID));
            verify(contactDao, times(1)).deleteById(anyLong());
        }
    }
}