package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.DataRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends DataRepository<Item> {

    List<Item> getAllByUser(long userId);

    List<Item> search(String text);
}