package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.errorHandler.exceptions.DuplicateUserException;
import ru.practicum.shareit.errorHandler.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto save(UserDto userDto) {
        User user = userRepository.save(UserMapper.toUser(userDto));
        log.info("User with id={} added successfully", user.getId());

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        User beingUpdated = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", id)));

        if (userDto.getEmail() != null) {
            checkForDuplication(userDto.getEmail());
            beingUpdated.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) beingUpdated.setName(userDto.getName());

        userRepository.save(beingUpdated);
        log.info("User with id={} updated successfully", id);

        return UserMapper.toUserDto(beingUpdated);
    }

    @Override
    public UserDto findById(long id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", id))));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(long id) {
        userRepository.deleteById(id);
        log.info("User with id={} deleted successfully", id);
    }

    private void checkForDuplication(String email) {
        if (userRepository.existsUserByEmail(email))
            throw new DuplicateUserException(String.format("User with email=%s already exists", email));
    }
}
