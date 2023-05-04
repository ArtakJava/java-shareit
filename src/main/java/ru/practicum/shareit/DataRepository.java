package ru.practicum.shareit;

import java.util.List;

public interface DataRepository<T extends DataEntity> {

    T create(T data);

    T get(long dataId);

    List<T> getAll();

    T update(T dataEntity, T dataPatch, Class<T> tClass) throws NoSuchFieldException, IllegalAccessException;

    void delete(long dataId);

    long generateDataId();

    void checkOfExist(T dataEntity);

    void validation(T dataEntity);

    T getUpdatedEntity(T dataEntity, T userPatch, Class<T> tClass) throws NoSuchFieldException, IllegalAccessException;
}