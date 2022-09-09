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
public class ItemDto {

    @NotNull(message = "Name must not be null")
    @NotBlank(message = "Name must not be blank")
    private String name;
    @NotNull(message = "Description must not be null")
    @NotBlank(message = "Description must not be blank")
    private String description;
    @NotNull(message = "Available must not be null")
    private Boolean available;
    private Long requestId;
}