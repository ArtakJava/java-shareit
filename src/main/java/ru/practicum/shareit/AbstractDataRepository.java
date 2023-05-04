package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractDataRepository<T extends DataEntity> implements DataRepository<T> {
    protected final Map<Long, T> dataStorage;
    protected long dataId;

    @Override
    public T create(T dataEntity) {
        validation(dataEntity);
        checkOfExist(dataEntity);
        dataEntity.setId(generateDataId());
        dataStorage.put(dataEntity.getId(), dataEntity);
        return dataEntity;
    }

    @Override
    public T get(long dataId) {
        return Optional.ofNullable(dataStorage.get(dataId))
                .orElseThrow(() -> new NotFoundException(InfoMessage.DATA_NOT_EXIST));
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(dataStorage.values());
    }

    @Override
    public T update(T dataEntity, T dataPatch, Class<T> tClass) throws NoSuchFieldException, IllegalAccessException {
        validation(dataPatch);
        T result = getUpdatedEntity(dataEntity, dataPatch, tClass);
        dataStorage.put(dataEntity.getId(), dataEntity);
        return result;
    }

    @Override
    public void delete(long dataId) {
        dataStorage.remove(dataId);
    }

    @Override
    public long generateDataId() {
        return ++dataId;
    }

    @Override
    public void checkOfExist(T dataEntity) {
        if (dataStorage.containsKey(dataEntity.getId())) {
            throw new AlreadyExistException(String.format(ErrorMessage.DATA_ALREADY_EXIST, dataEntity));
        }
    }

    @Override
    public T getUpdatedEntity(T dataEntity, T userPatch, Class<T> tClass) throws NoSuchFieldException, IllegalAccessException {
        Field[] dataFields = tClass.getSuperclass().getDeclaredFields();
        Field[] userFields = tClass.getDeclaredFields();
        for (Field field : dataFields) {
            Field userfield = tClass.getSuperclass().getDeclaredField(field.getName());
            userfield.setAccessible(true);
            if (userfield.get(userPatch) != null && !"id".equals(field.getName())) {
                userfield.set(dataEntity, userfield.get(userPatch));
            }
        }
        for (Field field : userFields) {
            Field userfield = tClass.getDeclaredField(field.getName());
            userfield.setAccessible(true);
            if (userfield.get(userPatch) != null) {
                userfield.set(
                        dataEntity, userfield.get(userPatch)
                );
            }
        }
        return dataEntity;
    }
}