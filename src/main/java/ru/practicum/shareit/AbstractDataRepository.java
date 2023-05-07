package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;

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
    public T update(T dataEntity, T dataPatch) {
        validation(dataPatch);
        T result = getUpdatedEntity(dataEntity, dataPatch);
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
}