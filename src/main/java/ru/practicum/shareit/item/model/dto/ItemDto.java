package ru.practicum.shareit.item.model.dto;

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
    private Long id;
    private Long ownerId;
    @NotNull(message = "Name may not be null")
    @NotBlank(message = "Name may not be blank")
    private String name;
    @NotNull(message = "Description may not be null")
    @NotBlank(message = "Description may not be blank")
    private String description;
    @NotNull(message = "Available may not be null")
    private Boolean available;
}
