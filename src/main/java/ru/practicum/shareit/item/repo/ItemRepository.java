package ru.practicum.shareit.item.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@EnableJpaRepositories
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findItemsByOwnerId(long ownerId);

    @Query(value = "select i from Item i where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))) and i.available = true")
    List<Item> search(String text);

    boolean existsByOwnerId(long ownerId);
}
