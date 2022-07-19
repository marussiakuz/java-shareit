package ru.practicum.shareit.requests;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Long id;
    private Long userId;
    private String name;
    private String purpose;
    private LocalDateTime creationTime;
}
