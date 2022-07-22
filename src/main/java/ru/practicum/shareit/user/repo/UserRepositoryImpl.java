package ru.practicum.shareit.user.repo;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.dto.UserDto;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, UserDto> users = new HashMap<>();

    @Override
    public void save(UserDto user) {
        users.put(user.getId(), user);
    }

    @Override
    public void update(UserDto user) {
        users.put(user.getId(), user);
    }

    @Override
    public UserDto findById(long id) {
        return users.get(id);
    }

    @Override
    public List<UserDto> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    @Override
    public boolean doesEmailExist(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public boolean doesUserExist(long id) {
        return users.containsKey(id);
    }
}
