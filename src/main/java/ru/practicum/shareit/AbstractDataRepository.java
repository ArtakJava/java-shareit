package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractDataRepository<T extends DataEntity> implements DataRepository<T> {
    protected final List<T> dataStorage;
    protected long dataId;

    @Override
    public T create(T dataEntity) {
        validation(dataEntity);
        dataEntity.setId(generateDataId());
        dataStorage.add(dataEntity);
        return dataEntity;
    }

    @Override
    public T get(long dataId) {
        return dataStorage.stream()
                .filter(data -> data.getId() == dataId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(InfoMessage.DATA_NOT_EXIST));
    }

    @Override
    public List<T> getAll() {
        return dataStorage;
    }

    @Override
    public T update(T oldDataEntity, T dataEntityPatched) {
        validation(dataEntityPatched);
        int index = dataStorage.indexOf(oldDataEntity);
        dataStorage.set(index, dataEntityPatched);
        return dataEntityPatched;
    }

    @Override
    public void delete(long dataId) {
        dataStorage.removeIf(data -> data.getId() == dataId);
    }

    protected long generateDataId() {
        return ++dataId;
    }

    protected void validation(T dataEntity) {
        if (dataStorage.contains(dataEntity)) {
            throw new AlreadyExistException(String.format(ErrorMessage.DATA_ALREADY_EXIST, dataEntity));
        }
    }
}