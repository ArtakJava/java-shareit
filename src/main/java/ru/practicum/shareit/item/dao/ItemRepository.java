package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.DataRepository;
import ru.practicum.shareit.item.model.ItemEntity;

import java.util.List;

public interface ItemRepository extends DataRepository<ItemEntity> {

    List<ItemEntity> getAllByUser(long userId);

    List<ItemEntity> search(String text);
}