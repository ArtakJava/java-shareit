package ru.practicum.shareit;

import java.util.List;

public interface DataRepository<T extends DataEntity> {

    T create(T data);

    T get(long dataId);

    List<T> getAll();

    T update(T oldData, T data);

    void delete(long dataId);
}