package ru.practicum.shareit.feedback.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    private Long id;
    private Long userId;
    private Long itemId;
    private String review;
    private Boolean useful;
}
