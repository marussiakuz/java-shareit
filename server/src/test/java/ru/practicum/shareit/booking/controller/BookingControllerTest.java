package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.errorHandler.ErrorHandler;
import ru.practicum.shareit.errorHandler.exceptions.BookingNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.InvalidRequestException;
import ru.practicum.shareit.errorHandler.exceptions.ItemNotFoundException;
import ru.practicum.shareit.errorHandler.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {
    @Autowired
    private BookingController bookingController;
    @MockBean
    private BookingService bookingService;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private static BookingInDto bookingInDto;
    private static BookingOutDto bookingOutDto;
    private static BookingOutDto approved;

    @BeforeAll
    public static void beforeAll() {
        bookingOutDto = BookingOutDto.builder()
                .id(1L)
                .item(Item.builder().id(1L).name("book").description("on java").available(true).build())
                .booker(User.builder().id(1L).name("booker").email("booker@gmail.com").build())
                .status("WAITING")
                .build();

        approved = BookingOutDto.builder()
                .id(1L)
                .item(Item.builder().id(1L).name("book").description("on java").available(true).build())
                .booker(User.builder().id(1L).name("booker").email("booker@gmail.com").build())
                .status("APPROVED")
                .build();

        bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(new ErrorHandler())
                .build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addValidBookingStatusIsOk() throws Exception {
        Mockito
                .when(bookingService.addNewBooking(2L, bookingInDto))
                .thenReturn(bookingOutDto);

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
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("booker"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("booker@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void addBookingByOwnerOfItemStatusIsNotFound() throws Exception {
        Mockito
                .when(bookingService.addNewBooking(1L, bookingInDto))
                .thenThrow(new ItemNotFoundException("the user trying to book his own item"));

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
        Mockito
                .when(bookingService.addNewBooking(1L, bookingInDto))
                .thenThrow(new ItemNotFoundException("Item with id=1 not found"));

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
        Mockito
                .when(bookingService.addNewBooking(5L, bookingInDto))
                .thenThrow(new UserNotFoundException("User with id=5 not found"));

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
        Mockito
                .when(bookingService.addNewBooking(2L, bookingInDto))
                .thenThrow(new InvalidRequestException("booking attempt failed due to incorrect data"));

        mockMvc.perform(post("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertEquals("booking attempt failed due to incorrect data",
                Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void updateStatusIsOk() throws Exception {
        Mockito
                .when(bookingService.updateStatus(1L, 1L, true))
                .thenReturn(approved);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("APPROVED"));
    }

    @Test
    void updateStatusNotExistsBookingStatusIsNotFound() throws Exception {
        Mockito
                .when(bookingService.updateStatus(3L, 1L, true))
                .thenThrow(new BookingNotFoundException("Booking with id=1 not found"));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Booking with id=1 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusThisUsersBookingsNotExistsStatusIsNotFound() throws Exception {
        Mockito
                .when(bookingService.updateStatus(3L, 1L, true))
                .thenThrow(new BookingNotFoundException("booking with id=1 for the user with id=3 was not found"));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("booking with id=1 for the user with id=3 was not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusWhichApprovedStatusIsBadRequest() throws Exception {
        Mockito
                .when(bookingService.updateStatus(1L, 1L, false))
                .thenThrow(new InvalidRequestException("the status cannot be changed"));

        mockMvc.perform(patch("/bookings/1?approved=false")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidRequestException))
                .andExpect(result -> assertEquals("the status cannot be changed",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingByIdStatusIsOk() throws Exception {
        Mockito
                .when(bookingService.getById(2L, 1L))
                .thenReturn(bookingOutDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.name").value("book"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.description").value("on java"))
                .andExpect(MockMvcResultMatchers.jsonPath("item.available").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.name").value("booker"))
                .andExpect(MockMvcResultMatchers.jsonPath("booker.email").value("booker@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("WAITING"));
    }

    @Test
    void getNotExistsBookingByIdStatusIsNotFound() throws Exception {
        Mockito
                .when(bookingService.getById(1L, 2L))
                .thenThrow(new BookingNotFoundException("Booking with id=2 not found"));

        mockMvc.perform(get("/bookings/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("Booking with id=2 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingByIdByUserNotOwnerAndNotBookerStatusIsNotFound() throws Exception {
        Mockito
                .when(bookingService.getById(3L, 1L))
                .thenThrow(new BookingNotFoundException("booking with id=1 for the user with id=3 was not found"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingNotFoundException))
                .andExpect(result -> assertEquals("booking with id=1 for the user with id=3 was not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookingsStatusIsOk() throws Exception {
        List<BookingOutDto> bookings = new ArrayList<>();
        BookingOutDto firstBooking = BookingOutDto.builder()
                .id(2L)
                .item(Item.builder().id(1L).build())
                .booker(User.builder().id(2L).build())
                .status("REJECTED")
                .build();
        BookingOutDto secondBooking = BookingOutDto.builder()
                .id(1L)
                .item(Item.builder().id(1L).build())
                .booker(User.builder().id(2L).build())
                .status("WAITING")
                .build();
        bookings.add(firstBooking);
        bookings.add(secondBooking);

        Mockito
                .when(bookingService.getUserBookings(2L, BookingState.ALL, 0, 10))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings?state=ALL&from=0&size=10")
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
    void getUserBookingsByNotExistsUserStatusIsNotFound() throws Exception {
        Mockito
                .when(bookingService.getUserBookings(5L, BookingState.ALL, 0, 10))
                .thenThrow(new UserNotFoundException("User with id=5 not found"));

        mockMvc.perform(get("/bookings?state=ALL&from=0&size=10")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=5 not found",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByOwnerIdStatusIsOk() throws Exception {
        BookingOutDto firstBooking = BookingOutDto.builder()
                .id(3L)
                .item(Item.builder().id(1L).build())
                .booker(User.builder().id(3L).build())
                .status("APPROVED")
                .build();
        BookingOutDto secondBooking = BookingOutDto.builder()
                .id(2L)
                .item(Item.builder().id(1L).build())
                .booker(User.builder().id(2L).build())
                .status("REJECTED")
                .build();
        BookingOutDto thirdBooking = BookingOutDto.builder()
                .id(1L)
                .item(Item.builder().id(1L).build())
                .booker(User.builder().id(2L).build())
                .status("WAITING")
                .build();
        List<BookingOutDto> bookings = new ArrayList<>(List.of(firstBooking, secondBooking, thirdBooking));

        Mockito
                .when(bookingService.getBookingsByOwnerId(1L, BookingState.ALL, 0, 10))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=10")
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
    void getBookingsByNotOwnerStatusIsNotFound() throws Exception {
        Mockito
                .when(bookingService.getBookingsByOwnerId(3L, BookingState.ALL, 0, 10))
                .thenThrow(new UserNotFoundException("User with id=3 is not the owner of any thing"));

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=10")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException))
                .andExpect(result -> assertEquals("User with id=3 is not the owner of any thing",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }
}