package ru.practicum.shareit.booking;

import lombok.Data;

import java.time.Period;

@Data
public class Booking {
    private Long id;
    private Long bookerId;
    private Long itemId;
    private Period period;
    private Boolean isConfirmed;
}
