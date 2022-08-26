package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.practicum.shareit.errorHandler.ErrorHandler;
import ru.practicum.shareit.errorHandler.exceptions.IllegalPaginationArgumentException;
import ru.practicum.shareit.errorHandler.exceptions.RequestNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.request.model.dto.RequestInDto;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.model.dto.UserDto;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase
@Sql({"/schema.sql"})
@AutoConfigureMockMvc
class RequestControllerTest {
    @Autowired
    private ItemController itemController;
    @Autowired
    private UserController userController;
    @Autowired
    private RequestController requestController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static UserDto userDto;
    private static RequestInDto requestInDto;

    @BeforeAll
    public static void beforeAll() {
        userDto = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();

        requestInDto = RequestInDto.builder()
                .description("I need book on java")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController, userController, requestController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }


    @Test
    void createValidRequestStatusIsOk() throws Exception {
        postUser(userDto);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("userId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("I need book on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("created").exists());
    }

    @Test
    void createRequestByNotExistsUserStatusIsNotFound() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=1 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void createRequestWithBlankDescriptionStatusIsBadRequest() throws Exception {
        RequestInDto blankDescription = RequestInDto.builder()
                .description(" ")
                .build();

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(blankDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByUserIdStatusIsOk() throws Exception {
        postUser(userDto);

        UserDto anotherUser = UserDto.builder()
                .name("another")
                .email("another@gmail.com")
                .build();
        postUser(anotherUser);

        RequestInDto first = RequestInDto.builder()
                .description("first")
                .build();
        postRequest(first, 1L);
        RequestInDto second = RequestInDto.builder()
                .description("second")
                .build();
        postRequest(second, 1L);
        RequestInDto another = RequestInDto.builder()
                .description("another")
                .build();
        postRequest(another, 2L);

        ItemDto itemToSecondRequest = ItemDto.builder()
                .requestId(2L)
                .available(true)
                .name("two")
                .description("very useful")
                .build();

        postItem(itemToSecondRequest, 2L);

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("second"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].ownerId").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].name").value("two"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].description").value("very useful"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].items.[0].available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("first"));
    }

    @Test
    void getAllByUserIdByNotExistsUserStatusIsNotFound() throws Exception {
        postUser(userDto);

        RequestInDto first = RequestInDto.builder()
                .description("first")
                .build();
        postRequest(first, 1L);
        RequestInDto second = RequestInDto.builder()
                .description("second")
                .build();
        postRequest(second, 1L);

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 3L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=3 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getAllAnotherUsersRequestsStatusIsOk() throws Exception {
        postUser(userDto);

        UserDto anotherUser = UserDto.builder()
                .name("another")
                .email("another@gmail.com")
                .build();
        postUser(anotherUser);

        RequestInDto first = RequestInDto.builder()
                .description("first")
                .build();
        postRequest(first, 1L);
        RequestInDto second = RequestInDto.builder()
                .description("second")
                .build();
        postRequest(second, 1L);
        RequestInDto another = RequestInDto.builder()
                .description("another")
                .build();
        postRequest(another, 2L);

        mockMvc.perform(get("/requests/all?from=0&size=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("second"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("first"));
    }

    @Test
    void getAllInvalidPageArgumentsStatusIsBadRequest() throws Exception {
        postUser(userDto);

        UserDto anotherUser = UserDto.builder()
                .name("another")
                .email("another@gmail.com")
                .build();
        postUser(anotherUser);

        RequestInDto first = RequestInDto.builder()
                .description("first")
                .build();
        postRequest(first, 1L);
        RequestInDto second = RequestInDto.builder()
                .description("second")
                .build();
        postRequest(second, 1L);
        RequestInDto another = RequestInDto.builder()
                .description("another")
                .build();
        postRequest(another, 2L);

        mockMvc.perform(get("/requests/all?from=0&size=-5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalPaginationArgumentException))
                .andExpect(result -> assertEquals("variable size must be greater than or equal to 1",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getByRequestIdStatusIsOk() throws Exception {
        postUser(userDto);
        postRequest(requestInDto, 1L);

        mockMvc.perform(get("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("I need book on java"));
    }

    @Test
    void getByRequestIdByNotExistsUserStatusIsNotFound() throws Exception {
        mockMvc.perform(get("/requests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=1 not found",
                                Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void getByNotExistsRequestIdStatusIsNotFound() throws Exception {
        postUser(userDto);

        mockMvc.perform(get("/requests/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RequestNotFoundException))
                .andExpect(result -> assertEquals("Request with id=5 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    private void postUser(UserDto userDto) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    private void postItem(ItemDto itemDto, long userId) throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    private void postRequest(RequestInDto requestInDto, long userId) throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk());
    }
}