package ru.practicum.shareit.item.controller;

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

import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.errorHandler.ErrorHandler;
import ru.practicum.shareit.errorHandler.exceptions.IllegalPaginationArgumentException;
import ru.practicum.shareit.errorHandler.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.dto.CommentDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.model.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
class ItemControllerTest {
    @Autowired
    private ItemController itemController;
    @Autowired
    private UserController userController;
    @Autowired
    private BookingController bookingController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static UserDto itemOwner;
    private static ItemDto itemDto;

    @BeforeAll
    public static void beforeAll() {
        itemOwner = UserDto.builder()
                .email("user@yandex.ru")
                .name("userName")
                .build();

        itemDto = ItemDto.builder()
                .ownerId(1L)
                .available(true)
                .name("book")
                .description("on java")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController, userController, bookingController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createValidItemStatusIsOk() throws Exception {
        postUser(itemOwner);

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("ownerId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("on java"));
    }

    @Test
    void createItemByNotExistsUserStatusIsNotFound() throws Exception {
        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItemWithBlankNameStatusIsBadRequest() throws Exception {
        postUser(itemOwner);

        ItemDto notValidName = ItemDto.builder()
                .name(" ")
                .description("good")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(notValidName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithBlankDescriptionStatusIsBadRequest() throws Exception {
        postUser(itemOwner);

        ItemDto emptyDescription = ItemDto.builder()
                .name("book")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(emptyDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithAvailableNullStatusIsBadRequest() throws Exception {
        postUser(itemOwner);

        ItemDto emptyDescription = ItemDto.builder()
                .name("book")
                .description("good")
                .build();

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(emptyDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postValidCommentStatusIsOk() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        postBooking(bookingInDto, 2L);

        Thread.sleep(3000L);

        CommentDto commentDto = CommentDto.builder()
                .text("it was very useful item")
                .authorName("author")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("text").value("it was very useful item"))
                .andExpect(MockMvcResultMatchers.jsonPath("created").exists());
    }

    @Test
    void postCommentIfBookingCurrentStatusIsBadRequest() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        postBooking(bookingInDto, 2L);

        CommentDto commentDto = CommentDto.builder()
                .text("it was very useful item")
                .authorName("author")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postEmptyCommentStatusIsBadRequest() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
        postBooking(bookingInDto, 2L);

        Thread.sleep(3000L);

        CommentDto commentDto = CommentDto.builder()
                .text(" ")
                .authorName("author")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateValidItemStatusIsOk() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        ItemDto updated = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("available").value(false));
    }

    @Test
    void updateItemNotOwnerStatusIsNotFound() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        ItemDto updated = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=2 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNotExistsItemStatusIsNotFound() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        ItemDto updated = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("Item with id=2 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByItemIdIsOk() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        mockMvc.perform(get("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("ownerId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("available").value(true));
    }

    @Test
    void getByItemIdNotExistsItemIsNotFound() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        mockMvc.perform(get("/items/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("Item with id=2 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUserIdStatusIsOk() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto anotherUser = UserDto.builder()
                .name("anotherUser")
                .email("a@gmail.com")
                .build();
        postUser(anotherUser);

        ItemDto ownerItem = ItemDto.builder()
                .name("good item")
                .description("very good")
                .available(true)
                .build();
        postItem(ownerItem, 1L);
        ItemDto anotherUserItem = ItemDto.builder()
                .available(true)
                .name("useful item")
                .description("very necessary")
                .build();
        postItem(anotherUserItem, 2L);

        mockMvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("good item"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("very good"));
    }

    @Test
    void getByUserIdInvalidPageArgumentsStatusIsBadRequest() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        mockMvc.perform(get("/items?from=-1&size=20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalPaginationArgumentException))
                .andExpect(result -> assertEquals("variable from must be greater than or equal to 0",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void searchStatusIsOk() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto anotherUser = UserDto.builder()
                .name("anotherUser")
                .email("a@gmail.com")
                .build();
        postUser(anotherUser);

        ItemDto ownerItem = ItemDto.builder()
                .name("good item")
                .description("very good")
                .available(true)
                .build();
        postItem(ownerItem, 1L);
        ItemDto anotherUserItem = ItemDto.builder()
                .available(true)
                .name("useful item")
                .description("very necessary")
                .build();
        postItem(anotherUserItem, 2L);

        mockMvc.perform(get("/items/search?text=gOOd&from=0&size=10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("good item"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("very good"));
    }

    @Test
    void searchInvalidPageArgumentsStatusIsBadRequest() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        mockMvc.perform(get("/items/search?text=JaVa&from=0&size=-2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalPaginationArgumentException))
                .andExpect(result -> assertEquals("variable size must be greater than or equal to 1",
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

    private void postBooking(BookingInDto bookingInDto, long userId) throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isOk());
    }
}