package ru.aston.servlets;

import ru.aston.dto.ContactDto;
import ru.aston.dto.EventDto;
import ru.aston.dto.EventResponseDto;
import ru.aston.dto.EventShortDto;
import ru.aston.exception.EntityNotFoundException;
import ru.aston.exception.HttpException;
import ru.aston.exception.Response;
import ru.aston.mapper.EventMapper;
import ru.aston.service.impl.ContactServiceImpl;
import ru.aston.service.impl.EventServiceImpl;
import ru.aston.util.GetProvider;
import ru.aston.util.ResponseSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.aston.testData.TestConstants;
import ru.aston.testUtil.TestGetProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;

import static ru.aston.util.Constants.X_SHARER_USER_ID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServletTest {

    @Mock
    private EventServiceImpl eventService;

    @Mock
    private ContactServiceImpl contactService;

    @Mock
    private ResponseSender responseSender;

    @InjectMocks
    private EventServlet eventServlet;

    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpServletRequest request;

    @Test
    void doPut_whenPathInfoIsParticipant_sendEventInResponse() throws SQLException {
        when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
        when(request.getPathInfo()).thenReturn(TestConstants.PARTICIPANT_PATH);
        when(eventService.addParticipant(anyLong(), anyLong())).thenReturn(new EventResponseDto());
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(EventResponseDto.class));

        eventServlet.doPut(request, response);

        verify(eventService).addParticipant(anyLong(), anyLong());
        verify(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(EventResponseDto.class));
    }

    @Test
    void doPut_whenPathIsIncorrect_throwsHttpException() throws SQLException {
        when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
        when(request.getPathInfo()).thenReturn(TestConstants.INCORRECT_PATH);
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        eventServlet.doPut(request, response);

        verify(eventService, never()).addParticipant(anyLong(), anyLong());
        verify(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doPut_whenThrowsSQLException_statusCodeIsBadRequest() throws SQLException {
        when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
        when(request.getPathInfo()).thenReturn(TestConstants.PARTICIPANT_PATH);
        when(eventService.addParticipant(anyLong(), anyLong())).thenThrow(new SQLException("message"));
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        eventServlet.doPut(request, response);

        verify(eventService).addParticipant(anyLong(), anyLong());
        verify(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doGet_whenPathInfoIsNull_sendAllEventsInResponse() throws SQLException {
        List<EventShortDto> eventDtos =
                List.of(EventMapper.toShortDto(TestGetProvider.getEvent(TestConstants.FIRST_EVENT_TITLE, TestConstants.FIRST_EVENT_DESCRIPTION,
                        TestGetProvider.getUser(TestConstants.FIRST_USER_NAME, TestConstants.FIRST_USER_EMAIL))));

        when(request.getPathInfo()).thenReturn(null);
        when(eventService.getEvents(isNull())).thenReturn(eventDtos);
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(List.class));

        eventServlet.doGet(request, response);

        verify(eventService, times(1)).getEvents(isNull());
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(List.class));
    }

    @Test
    void doGet_whenPathInfoIsNotNull_sendOnlyEventInResponse() throws SQLException {
        List<EventShortDto> eventDtos =
                List.of(EventMapper.toShortDto(TestGetProvider.getEvent(TestConstants.FIRST_EVENT_TITLE, TestConstants.FIRST_EVENT_DESCRIPTION,
                        TestGetProvider.getUser(TestConstants.FIRST_USER_NAME, TestConstants.FIRST_USER_EMAIL))));

        when(request.getPathInfo()).thenReturn("path info");
        when(eventService.getEvents(anyString())).thenReturn(eventDtos);
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(EventShortDto.class));

        eventServlet.doGet(request, response);

        verify(eventService, times(1)).getEvents(anyString());
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(EventShortDto.class));
    }

    @Test
    void doGet_whenThrowsEntityNotFoundException_statusCodeIsBedRequest() throws SQLException {
        when(request.getPathInfo()).thenReturn(null);
        when(eventService.getEvents(isNull())).thenThrow(new EntityNotFoundException("message"));
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        eventServlet.doGet(request, response);

        verify(eventService, times(1)).getEvents(isNull());
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doGet_whenThrowsHttpException_statusCodeIsInternalServerError() throws SQLException {
        when(request.getPathInfo()).thenReturn(null);
        when(eventService.getEvents(isNull())).thenThrow(new HttpException("message"));
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        eventServlet.doGet(request, response);

        verify(eventService, times(1)).getEvents(isNull());
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doPost_whenPathInfoIsEmpty_sendNewEventInResponse() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.EVENT_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
            when(request.getPathInfo()).thenReturn(null);
            when(eventService.saveEvent(any(EventDto.class), anyLong())).thenReturn(new EventResponseDto());
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(EventResponseDto.class));

            eventServlet.doPost(request, response);

            verify(eventService, times(1)).saveEvent(any(EventDto.class), anyLong());
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(EventResponseDto.class));
        }
    }

    @Test
    void doPost_whenEventJsonIsIncorrect_statusCodeIsBadRequest() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.INCORRECT_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
            when(request.getPathInfo()).thenReturn(null);
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            eventServlet.doPost(request, response);

            verify(eventService, never()).saveEvent(any(EventDto.class), anyLong());
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doPost_whenPathInfoIsContact_sendNewContactInResponse() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.CONTACT_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
            when(request.getPathInfo()).thenReturn(TestConstants.CONTACT_PATH);
            when(contactService.saveContact(any(ContactDto.class), anyLong(), anyLong())).thenReturn(new ContactDto());
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(ContactDto.class));

            eventServlet.doPost(request, response);

            verify(eventService, never()).saveEvent(any(EventDto.class), anyLong());
            verify(contactService, times(1)).saveContact(any(ContactDto.class), anyLong(), anyLong());
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(ContactDto.class));
        }
    }

    @Test
    void doPost_whenContactJsonIsIncorrect_statusCodeIsBedRequest() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.INCORRECT_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
            when(request.getPathInfo()).thenReturn(TestConstants.CONTACT_PATH);
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            eventServlet.doPost(request, response);

            verify(eventService, never()).saveEvent(any(EventDto.class), anyLong());
            verify(contactService, never()).saveContact(any(ContactDto.class), anyLong(), anyLong());
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doPost_whenThrowsSQLException_statusCodeIsBadRequest() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.EVENT_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
            when(request.getPathInfo()).thenReturn(null);
            when(eventService.saveEvent(any(EventDto.class), anyLong())).thenThrow(new SQLException());
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            eventServlet.doPost(request, response);

            verify(eventService, times(1)).saveEvent(any(EventDto.class), anyLong());
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doPost_whenThrowsHttpException_statusCodeIsInternalServerError() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.EVENT_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
            when(request.getPathInfo()).thenReturn(null);
            when(eventService.saveEvent(any(EventDto.class), anyLong())).thenThrow(new HttpException("message"));
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            eventServlet.doPost(request, response);

            verify(eventService, times(1)).saveEvent(any(EventDto.class), anyLong());
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doPost_whenHeaderIsNotNumber_statusCodeIsBadRequest() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.EVENT_JSON);
            when(request.getHeader(X_SHARER_USER_ID)).thenReturn("NOT NUMBER");
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            eventServlet.doPost(request, response);

            verify(eventService, never()).saveEvent(any(EventDto.class), anyLong());
            verify(contactService, never()).saveContact(any(ContactDto.class), anyLong(), anyLong());
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doDelete() throws SQLException {
        when(request.getPathInfo()).thenReturn("/" + TestConstants.FIRST_ID);
        when(request.getHeader(X_SHARER_USER_ID)).thenReturn(String.valueOf(TestConstants.FIRST_ID));
        when(eventService.deleteEvent(anyString(), anyLong())).thenReturn(TestConstants.FIRST_ID);
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        eventServlet.doDelete(request, response);

        verify(eventService).deleteEvent(anyString(), anyLong());
        verify(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }
}