package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.practicum.shareit.errorHandler.ErrorHandler;
import ru.practicum.shareit.errorHandler.exceptions.DuplicateUserException;
import ru.practicum.shareit.errorHandler.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.dto.UserDto;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase
@Sql({"/schema.sql"})
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private UserController userController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static UserDto userDto;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void createValidUserStatusIsOk() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("userName"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("user@yandex.ru"));
    }

    @Test
    void createUserWithInvalidEmailStatusIsBadRequest() throws Exception {
        UserDto invalidEmail = UserDto.builder()
                .name("User")
                .email("us")
                .build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateValidUserStatusIsOk() throws Exception {
        postUser(userDto);

        UserDto updated = UserDto.builder()
                .name("New")
                .email("new@ya.ru")
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("New"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("new@ya.ru"));
    }

    @Test
    void updateUserDuplicateEmailStatusIsConflict() throws Exception {
        UserDto userDtoWithTheSameEmail = UserDto.builder()
                .name("name")
                .email("new@ya.ru")
                .build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDtoWithTheSameEmail)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("new@ya.ru"));

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("user@yandex.ru"));

        UserDto updated = UserDto.builder()
                .name("New")
                .email("new@ya.ru")
                .build();

        mockMvc.perform(patch("/users/2")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateUserException))
                .andExpect(result -> assertEquals("User with email=new@ya.ru already exists",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isConflict());
    }

    @Test
    void updateNotExistsUserStatusIsNotFound() throws Exception {
        postUser(userDto);

        UserDto updated = UserDto.builder()
                .name("New")
                .email("new@ya.ru")
                .build();

        mockMvc.perform(patch("/users/10")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=10 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findValidUserByIdStatusIsOk() throws Exception {
        postUser(userDto);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("userName"))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value("user@yandex.ru"));
    }

    @Test
    void findNotExistsUserByIdStatusIsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/1"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=1 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll() throws Exception {
        postUser(userDto);

        UserDto secondUserDto = UserDto.builder()
                .email("second@yandex.ru")
                .name("two")
                .build();
        postUser(secondUserDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("userName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("user@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("two"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].email").value("second@yandex.ru"));
    }

    @Test
    void deleteValidUserByIdStatusIsOk() throws Exception {
        postUser(userDto);

        mockMvc.perform(delete("/users/1")
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    private void postUser(UserDto userDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }
}