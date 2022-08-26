package ru.practicum.shareit.booking.model.dto;

import lombok.*;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class BookingShortDto {
    private Long id;
    @ManyToOne
    @JoinColumn(name = "booker_id")
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}
