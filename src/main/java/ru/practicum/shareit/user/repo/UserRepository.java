package ru.practicum.shareit.user.repo;

import ru.practicum.shareit.user.model.dto.UserDto;

import java.util.List;

public interface UserRepository {
    void save(UserDto user);
    void update(UserDto user);
    UserDto findById(long id);
    List<UserDto> findAll();
    void delete(long id);
    boolean doesEmailExist(String email);
    boolean doesUserExist(long id);
}
