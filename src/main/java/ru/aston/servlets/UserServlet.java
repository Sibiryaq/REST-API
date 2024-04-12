package ru.aston.servlets;

import ru.aston.dto.UserDto;
import ru.aston.exception.EntityNotFoundException;
import ru.aston.exception.HttpException;
import ru.aston.exception.Response;
import ru.aston.service.UserService;
import ru.aston.service.impl.UserServiceImpl;
import ru.aston.util.GetProvider;
import ru.aston.util.ResponseSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/user/*")
@Slf4j
public class UserServlet extends HttpServlet {

    private final UserService userService;
    private final ResponseSender responseSender;

    public UserServlet() {
        this.userService = new UserServiceImpl(GetProvider.getUserDao());
        this.responseSender = new ResponseSender();
    }

    public UserServlet(UserService userService, ResponseSender responseSender) {
        this.userService = userService;
        this.responseSender = responseSender;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        UserDto savedUserDto;
        String body = GetProvider.getBody(req);

        try {
            UserDto userDto = userDtoFromJson(body, resp);
            savedUserDto = userService.saveUser(userDto);
            responseSender.sendResponse(resp, HttpServletResponse.SC_CREATED, savedUserDto);
        } catch (SQLException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
        } catch (HttpException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            List<UserDto> users = userService.getAllUsers();
            responseSender.sendResponse(resp, HttpServletResponse.SC_OK, users);
        } catch (SQLException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
        } catch (HttpException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String requestPath = req.getPathInfo();
        try {
            Long id = userService.deleteUser(requestPath);
            responseSender.sendResponse(resp, HttpServletResponse.SC_OK,
                    new Response(String.format("User with id %d was successfully deleted", id)));
        } catch (SQLException | EntityNotFoundException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
        } catch (HttpException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            Response response = new Response(e.getMessage());
            responseSender.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    private UserDto userDtoFromJson(String body, HttpServletResponse resp) {
        UserDto userDto = null;
        try {
            userDto = GetProvider.getObjectMapper().readValue(body, UserDto.class);
        } catch (JsonProcessingException e) {
            log.warn(Arrays.toString(e.getStackTrace()));
            responseSender.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new Response("Incorrect Json received"));
        }
        return userDto;
    }
}
