package ru.aston.repository;

import ru.aston.model.User;
import ru.aston.repository.impl.UserDaoImpl;
import ru.aston.util.GetProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static ru.aston.testData.TestConstants.*;
import static ru.aston.testUtil.TestGetProvider.getUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserDaoImplTest extends TestBaseDao {

    private UserDaoImpl userDao;

    private User firstUser;

    @BeforeEach
    void setUp() {
        userDao = (UserDaoImpl) GetProvider.getUserDao();
        userDao.setConnectionBuilder(getConnectionBuilder());
        firstUser = getUser(FIRST_USER_NAME, FIRST_USER_EMAIL);
    }


    @Test
    void saveUser_usualCase() throws SQLException {
        Optional<User> savedUserOpt = userDao.save(firstUser);

        assertThat(savedUserOpt.isPresent(), equalTo(true));
        User savedUser = savedUserOpt.get();

        assertThat(savedUser.getId(), equalTo(FIRST_ID));
        assertThat(savedUser.getName(), equalTo(FIRST_USER_NAME));
        assertThat(savedUser.getEmail(), equalTo(FIRST_USER_EMAIL));

        userDao.deleteById(savedUser.getId());
    }

    @Test
    void saveUser_whenEmailExist_ThrowPSQLException() throws SQLException {
        Optional<User> savedUserOpt = userDao.save(firstUser);

        User secondUser = getUser(SECOND_USER_NAME, SECOND_USER_EMAIL);
        secondUser.setEmail(FIRST_USER_EMAIL);

        Exception exception = assertThrows(SQLException.class, () ->
                userDao.save(secondUser));

        assertThat(exception.getClass(), equalTo(PSQLException.class));

        userDao.deleteById(savedUserOpt.get().getId());
    }

    @Test
    void saveUser_whenNameIsNull_ThrowPSQLException() {
        firstUser.setName(null);

        Exception exception = assertThrows(SQLException.class, () ->
                userDao.save(firstUser));

        assertThat(exception.getClass(), equalTo(PSQLException.class));
    }

    @Test
    void saveUser_whenEmailIsNull_ThrowPSQLException() {
        firstUser.setEmail(null);

        Exception exception = assertThrows(SQLException.class, () ->
                userDao.save(firstUser));

        assertThat(exception.getClass(), equalTo(PSQLException.class));
    }

    @Test
    void findUserById_usualCase() throws SQLException {
        Optional<User> savedUserOpt = userDao.save(firstUser);

        Optional<User> resultOpt = userDao.findById(savedUserOpt.get().getId());

        assertThat(resultOpt.isPresent(), equalTo(true));
        assertThat(resultOpt.get().getName(), equalTo(FIRST_USER_NAME));
        assertThat(resultOpt.get().getEmail(), equalTo(FIRST_USER_EMAIL));

        userDao.deleteById(savedUserOpt.get().getId());
    }

    @Test
    void findUserById_whenIdNotFound_returnNull() throws SQLException {
        Optional<User> resultOpt = userDao.findById(FIRST_ID);

        Assertions.assertFalse(resultOpt.isPresent());
    }

    @Test
    void findAllUsers_usualCase_returnUsersList() throws SQLException {
        Optional<User> savedUserOpt = userDao.save(firstUser);

        User secondUser = getUser(SECOND_USER_NAME, SECOND_USER_EMAIL);
        Optional<User> savedUserOpt2 = userDao.save(secondUser);

        List<User> result = userDao.findAll();

        userDao.deleteById(savedUserOpt.get().getId());
        userDao.deleteById(savedUserOpt2.get().getId());

        assertThat(result, hasSize(2));
    }

    @Test
    void updateUserName_usualCase() throws SQLException {
        User savedUser = userDao.save(firstUser).get();
        savedUser.setName(UPDATED_USER_NAME);

        userDao.update(savedUser);
        Optional<User> updatedUser = userDao.findById(savedUser.getId());
        userDao.deleteById(savedUser.getId());

        assertThat(updatedUser.isPresent(), equalTo(true));
        assertThat(updatedUser.get().getName(), equalTo(UPDATED_USER_NAME));
        assertThat(updatedUser.get().getEmail(), equalTo(FIRST_USER_EMAIL));
    }

    @Test
    void updateUserEmail_usualCase() throws SQLException {
        User savedUser = userDao.save(firstUser).get();
        savedUser.setEmail(UPDATED_USER_EMAIL);

        userDao.update(savedUser);
        Optional<User> updatedUser = userDao.findById(savedUser.getId());
        userDao.deleteById(savedUser.getId());

        assertThat(updatedUser.isPresent(), equalTo(true));
        assertThat(updatedUser.get().getName(), equalTo(FIRST_USER_NAME));
        assertThat(updatedUser.get().getEmail(), equalTo(UPDATED_USER_EMAIL));
    }
}
