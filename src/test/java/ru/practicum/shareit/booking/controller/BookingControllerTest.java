package ru.practicum.shareit.booking.controller;

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

import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.errorHandler.ErrorHandler;
import ru.practicum.shareit.errorHandler.exceptions.BookingNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.InvalidRequestException;
import ru.practicum.shareit.errorHandler.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
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
class BookingControllerTest {
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
    void addValidBookingStatusIsOk() throws Exception {
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

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("booker"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("booker@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void addBookingByOwnerOfItemStatusIsNotFound() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .build();

        mapper.registerModule(new JavaTimeModule());

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("the user trying to book his own item",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingNotExistsItemStatusIsNotFound() throws Exception {
        postUser(itemOwner);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemNotFoundException))
                .andExpect(result -> assertEquals("Item with id=1 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingByNotExistsUserStatusIsNotFound() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 5L)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=5 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingNotAvailableItemStatusIsBadRequest() throws Exception {
        postUser(itemOwner);

        ItemDto notAvailableItem = ItemDto.builder()
                .name("book")
                .description("Thinking on java")
                .available(false)
                .build();
        postItem(notAvailableItem, 1L);

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

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusIsOk() throws Exception {
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

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("APPROVED"));
    }

    @Test
    void updateStatusNotExistsBookingStatusIsNotFound() throws Exception {
        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Booking with id=1 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusThisUsersBookingsNotExistsStatusIsNotFound() throws Exception {
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

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("booking with id=1 for the user with id=3 was not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusWhichApprovedStatusIsBadRequest() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .build();
        postBooking(bookingInDto, 2L);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("APPROVED"));

        mockMvc.perform(patch("/bookings/1?approved=false")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(result -> assertEquals("the status cannot be changed",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotExistsBookingByIdStatusIsOk() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingInDto, 2L);

        mockMvc.perform(get("/bookings/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("booker"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("booker@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("start").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("end").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void getBookingByIdStatusIsOk() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingInDto, 2L);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("booker"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("booker@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("start").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("end").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void getNotExistsBookingByIdStatusIsNotFound() throws Exception {
        postUser(itemOwner);

        mockMvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Booking with id=2 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingByIdByUserNotOwnerAndNotBookerStatusIsNotFound() throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(bookingInDto, 2L);

        UserDto notBookerNotOwner = UserDto.builder()
                .name("somebody")
                .email("smbd@gmail.com")
                .build();
        postUser(notBookerNotOwner);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("booking with id=1 for the user with id=3 was not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookingsStatusIsOk () throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        UserDto secondBooker = UserDto.builder()
                .name("somebody")
                .email("smbd@gmail.com")
                .build();
        postUser(secondBooker);

        BookingInDto firstBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(firstBooking, 2L);
        BookingInDto secondBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(secondBooking, 2L);
        BookingInDto bookingOfSecondBooker = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .build();
        postBooking(bookingOfSecondBooker, 3L);

        mockMvc.perform(patch("/bookings/2?approved=false")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("REJECTED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value("WAITING"));
    }

    @Test
    void getUserBookingsStateNotValidStatusIsBadRequest () throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto firstBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(firstBooking, 2L);
        BookingInDto secondBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(secondBooking, 2L);

        mockMvc.perform(get("/bookings?state=EVERY")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(result -> assertEquals("Unknown state: UNSUPPORTED_STATUS",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookingsByNotExistsUserStatusIsNotFound () throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);

        BookingInDto firstBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(firstBooking, 2L);
        BookingInDto secondBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(secondBooking, 2L);

        mockMvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=5 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByOwnerIdStatusIsOk () throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);
        UserDto secondBooker = UserDto.builder()
                .name("somebody")
                .email("smbd@gmail.com")
                .build();
        postUser(secondBooker);

        BookingInDto firstBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(firstBooking, 2L);
        BookingInDto secondBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(secondBooking, 2L);

        mockMvc.perform(patch("/bookings/2?approved=false")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        BookingInDto bookingOfSecondBooker = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .build();
        postBooking(bookingOfSecondBooker, 3L);

        mockMvc.perform(patch("/bookings/3?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner?state=ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].booker.id").value("3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("APPROVED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].status").value("REJECTED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].booker.id").value("2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].status").value("WAITING"));
    }

    @Test
    void getBookingsByNotOwnerStatusIsNotFound () throws Exception {
        postUser(itemOwner);
        postItem(itemDto, 1L);

        UserDto booker = UserDto.builder()
                .name("booker")
                .email("booker@gmail.com")
                .build();
        postUser(booker);
        UserDto notOwner = UserDto.builder()
                .name("somebody")
                .email("smbd@gmail.com")
                .build();
        postUser(notOwner);

        BookingInDto firstBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        postBooking(firstBooking, 2L);
        BookingInDto secondBooking = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        postBooking(secondBooking, 2L);

        mockMvc.perform(get("/bookings/owner?state=ALL")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=3 is not the owner of any thing",
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