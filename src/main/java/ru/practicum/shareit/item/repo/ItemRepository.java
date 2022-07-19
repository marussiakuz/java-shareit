package ru.practicum.shareit.item.repo;

import ru.practicum.shareit.item.model.dto.ItemDto;

import java.util.List;

public interface ItemRepository {
    void save(ItemDto item);
    void update(ItemDto item);
    ItemDto getById(long itemId);
    List<ItemDto> findByUserId(long userId);
    List<ItemDto> search(String text);
}
