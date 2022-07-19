package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    private Long id;
    private Long bookerId;
    private Long itemId;
    private Period period;
    private Status status;
}
