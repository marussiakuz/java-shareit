package ru.practicum.shareit.booking.dto;

import java.time.Period;

public class BookingDto {
    private Long id;
    private Long bookerId;
    private Long itemId;
    private Period period;
    private Boolean isConfirmed;
}
