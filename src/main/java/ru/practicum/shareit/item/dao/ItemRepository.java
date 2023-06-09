package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Set;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByUserOrderById(User user);

    List<Item> findByNameContainingIgnoreCaseAndAvailableTrueOrDescriptionContainingIgnoreCaseAndAvailableTrue(
            String text, String text1
    );

    List<Item> findByRequestIdIn(Set<Long> collect);

    List<Item> findByRequestId(long id);
}