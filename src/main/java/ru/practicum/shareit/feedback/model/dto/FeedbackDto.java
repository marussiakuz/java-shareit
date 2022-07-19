package ru.practicum.shareit.feedback.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDto {
    private Long id;
    private Long userId;
    private Long itemId;
    private String review;
    private Boolean useful;
}
