package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.model.dto.BookingDto;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBookerId())
                .itemId(booking.getItemId())
                .period(booking.getPeriod())
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .bookerId(bookingDto.getBookerId())
                .itemId(bookingDto.getItemId())
                .period(bookingDto.getPeriod())
                .status(bookingDto.getStatus())
                .build();
    }
}
