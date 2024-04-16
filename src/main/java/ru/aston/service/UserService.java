package ru.aston.service;

import ru.aston.dto.UserDto;

import java.sql.SQLException;
import java.util.List;

public interface UserService {

    UserDto saveUser(UserDto userDto) throws SQLException;

    List<UserDto> getAllUsers() throws SQLException;

    Long deleteUser(String requestPath) throws SQLException;
}
