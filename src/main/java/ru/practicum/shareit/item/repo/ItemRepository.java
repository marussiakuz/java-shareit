package ru.practicum.shareit.item.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@EnableJpaRepositories
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findItemsByOwnerId(long ownerId);
    List<Item> findItemsByNameContainsIgnoreCaseOrDescriptionContainsIgnoreCaseAndAvailableTrue(String name,
                                                                                                String description);
    boolean existsByOwnerId(long ownerId);
}
