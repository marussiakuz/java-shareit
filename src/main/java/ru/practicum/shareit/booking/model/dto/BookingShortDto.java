package ru.practicum.shareit.booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Getter
@Setter
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
