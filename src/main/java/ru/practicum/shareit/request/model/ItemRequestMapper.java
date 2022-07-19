package ru.practicum.shareit.request.model;

import ru.practicum.shareit.request.model.dto.ItemRequestDto;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .userId(itemRequest.getUserId())
                .description(itemRequest.getDescription())
                .creationTime(itemRequest.getCreationTime())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .userId(itemRequestDto.getUserId())
                .description(itemRequestDto.getDescription())
                .creationTime(itemRequestDto.getCreationTime())
                .build();
    }
}
