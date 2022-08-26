package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.booking.model.dto.BookingInDto;
import ru.practicum.shareit.booking.model.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingOutDto addNewBooking(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                       @Valid @RequestBody BookingInDto bookingInDto) {
        return bookingService.addNewBooking(userId, bookingInDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto update(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId,
                                @RequestParam(value = "approved") boolean isApproved) {
        return bookingService.updateStatus(userId, bookingId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto getById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(value = "state", required = false, defaultValue = "ALL")
                                                   String state,
                                               @RequestParam(value = "from", required = false, defaultValue = "0")
                                                   int from,
                                               @RequestParam(value = "size", required = false, defaultValue = "10")
                                                   int size) {
        return bookingService.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> getBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                    @RequestParam(value = "state", required = false, defaultValue = "ALL")
                                                        String state,
                                                    @RequestParam (value = "from", required = false, defaultValue = "0")
                                                        int from,
                                                    @RequestParam (value = "size", required = false, defaultValue = "10")
                                                        int size) {
        return bookingService.getBookingsByOwnerId(userId, state, from, size);
    }
}
