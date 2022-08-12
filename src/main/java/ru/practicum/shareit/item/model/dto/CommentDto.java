package ru.practicum.shareit.item.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    @NotNull(message = "Text may not be null")
    @NotBlank(message = "Text may not be blank")
    private String text;
    private String authorName;
    private LocalDateTime created;
}
