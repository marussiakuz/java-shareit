package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    @NotNull(message = "Text must not be null")
    @NotBlank(message = "Text must not be blank")
    private String text;
    private String authorName;
}
