package ru.aston.servlets;

import ru.aston.dto.UserDto;
import ru.aston.exception.EntityNotFoundException;
import ru.aston.exception.HttpException;
import ru.aston.exception.Response;
import ru.aston.mapper.UserMapper;
import ru.aston.service.impl.UserServiceImpl;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServletTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private ResponseSender responseSender;

    @InjectMocks
    private UserServlet userServlet;

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;


    @Test
    void doPost_whenJsonIsCorrect_statusCodeIsCreated() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            UserDto userDto = UserMapper.toDto(TestGetProvider.getUser(TestConstants.FIRST_USER_NAME, TestConstants.FIRST_USER_EMAIL));
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.USER_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(userService.saveUser(any(UserDto.class))).thenReturn(userDto);
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(UserDto.class));

            userServlet.doPost(httpServletRequest, httpServletResponse);

            verify(userService, times(1)).saveUser(any(UserDto.class));
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(UserDto.class));
        }
    }

    @Test
    void doPost_whenThrownSQLException_statusCodeIsBedRequest() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.USER_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(userService.saveUser(any(UserDto.class))).thenThrow(new SQLException());
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            userServlet.doPost(httpServletRequest, httpServletResponse);

            verify(userService, times(1)).saveUser(any(UserDto.class));
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doPost_whenThrownHttpException_statusCodeIsInternalServerError() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.USER_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            when(userService.saveUser(any(UserDto.class))).thenThrow(new HttpException("message"));
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            userServlet.doPost(httpServletRequest, httpServletResponse);

            verify(userService, times(1)).saveUser(any(UserDto.class));
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doPost_whenJsonIsIncorrect_statusCodeBedRequest() throws SQLException {
        try (MockedStatic<GetProvider> getProvider = mockStatic(GetProvider.class)) {
            getProvider.when(() -> GetProvider.getBody(any(HttpServletRequest.class))).thenReturn(TestConstants.INCORRECT_JSON);
            getProvider.when(GetProvider::getObjectMapper).thenReturn(new ObjectMapper());
            doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

            userServlet.doPost(httpServletRequest, httpServletResponse);

            verify(userService, never()).saveUser(any(UserDto.class));
            verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
        }
    }

    @Test
    void doGet_whenStatusCodeIsOk() throws SQLException {
        List<UserDto> expected = List.of(UserMapper.toDto(TestGetProvider.getUser(TestConstants.FIRST_USER_NAME, TestConstants.FIRST_USER_EMAIL)));
        when(userService.getAllUsers()).thenReturn(expected);
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(List.class));

        userServlet.doGet(httpServletRequest, httpServletResponse);

        verify(userService, times(1)).getAllUsers();
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(List.class));
    }

    @Test
    void doGet_whenStatusCodeIsBadRequest() throws SQLException {
        when(userService.getAllUsers()).thenThrow(new SQLException());
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        userServlet.doGet(httpServletRequest, httpServletResponse);

        verify(userService, times(1)).getAllUsers();
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doGet_whenStatusCodeIsInternalServerError() throws SQLException {
        when(userService.getAllUsers()).thenThrow(new HttpException("message"));
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        userServlet.doGet(httpServletRequest, httpServletResponse);

        verify(userService, times(1)).getAllUsers();
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doDelete_whenStatusCodeIsOk() throws SQLException {
        when(httpServletRequest.getPathInfo()).thenReturn("path");
        when(userService.deleteUser(anyString())).thenReturn(TestConstants.FIRST_ID);
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        userServlet.doDelete(httpServletRequest, httpServletResponse);

        verify(httpServletRequest, times(1)).getPathInfo();
        verify(userService, times(1)).deleteUser(anyString());
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doDelete_whenStatusCodeIsBadRequest() throws SQLException {
        when(httpServletRequest.getPathInfo()).thenReturn("path");
        when(userService.deleteUser(anyString())).thenThrow(new EntityNotFoundException("message"));
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        userServlet.doDelete(httpServletRequest, httpServletResponse);

        verify(httpServletRequest, times(1)).getPathInfo();
        verify(userService, times(1)).deleteUser(anyString());
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }

    @Test
    void doDelete_whenStatusCodeIsInternalServerError() throws SQLException {
        when(httpServletRequest.getPathInfo()).thenReturn("path");
        when(userService.deleteUser(anyString())).thenThrow(new HttpException("message"));
        doNothing().when(responseSender).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));

        userServlet.doDelete(httpServletRequest, httpServletResponse);

        verify(httpServletRequest, times(1)).getPathInfo();
        verify(userService, times(1)).deleteUser(anyString());
        verify(responseSender, times(1)).sendResponse(any(HttpServletResponse.class), anyInt(), any(Response.class));
    }
}