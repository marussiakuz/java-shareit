package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.errorHandler.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repo.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingOutDto addNewBooking(long userId, BookingInDto bookingInDto) {
        Item item = itemRepository.findById(bookingInDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(String.format("Item with id=%s not found",
                        bookingInDto.getItemId())));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", userId)));

        LocalDateTime start = bookingInDto.getStart();
        LocalDateTime end = bookingInDto.getEnd();
        if (userId == item.getOwner().getId()) throw new ItemNotFoundException("the user trying to book his own item");
        if (!item.getAvailable() || start.isBefore(LocalDateTime.now()) || start.isAfter(end))
            throw new InvalidRequestException("booking attempt failed due to incorrect data");

        bookingInDto.setStatus(Status.WAITING.getStatus());
        return BookingMapper.toBookingDto(bookingRepository.save(BookingMapper.toBooking(bookingInDto, user, item)));
    }

    @Override
    public BookingOutDto updateStatus(long userId, long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(String.format("Booking with id=%s not found", bookingId)));

        if (userId != booking.getItem().getOwner().getId())
            throw new BookingNotFoundException(String.format("booking with id=%s for the user with id=%s was not found",
                    bookingId, userId));
        if (booking.getStatus() == Status.APPROVED || booking.getStatus() == Status.REJECTED)
            throw new InvalidRequestException("the status cannot be changed");

        booking.setStatus(isApproved ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingOutDto getById(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(String.format("Booking with id=%s not found", bookingId)));

        if (userId != booking.getItem().getOwner().getId() && userId != booking.getBooker().getId())
            throw new BookingNotFoundException(String.format("booking with id=%s for the user with id=%s was not found",
                    bookingId, userId));

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingOutDto> getUserBookings(long userId, String state) {
        checkValidState(state);

        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(String.format("User with id=%s not found", userId));

        return getFilteredBookingsByState(bookingRepository.getBookingsByBookerIdOrderByStartDesc(userId),
                State.valueOf(state));
    }

    @Override
    public List<BookingOutDto> getBookingsByOwnerId(long ownerId, String state) {
        checkValidState(state);

        if (!itemRepository.existsByOwnerId(ownerId))
            throw new UserNotFoundException(String.format("User with id=%s is not the owner of any thing", ownerId));

        return getFilteredBookingsByState(bookingRepository.getBookingsByOwnerId(ownerId), State.valueOf(state));
    }

    private List<BookingOutDto> getFilteredBookingsByState(List<Booking> bookings, State state) {
        List<BookingOutDto> bookingsDto = bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());

        if (state == State.ALL) return bookingsDto;

        return bookingsDto.stream()
                .filter(bookingOutDto -> filterByState(bookingOutDto, state))
                .collect(Collectors.toList());
    }

    private boolean filterByState(BookingOutDto bookingOutDto, State state) {
        switch (state.getState()) {
            case "CURRENT": return bookingOutDto.getStart().isBefore(LocalDateTime.now())
                    && bookingOutDto.getEnd().isAfter(LocalDateTime.now());
            case "FUTURE": return bookingOutDto.getStart().isAfter(LocalDateTime.now());
            case "PAST": return bookingOutDto.getEnd().isBefore(LocalDateTime.now());
            default: return Objects.equals(bookingOutDto.getStatus(), state.getState());
        }
    }

    private void checkValidState(String state) {
        if (Arrays.stream(State.values()).noneMatch(element -> Objects.equals(element.getState(), state)))
            throw new InvalidRequestException("Unknown state: UNSUPPORTED_STATUS");
    }
}
