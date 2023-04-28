package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.AbstractDataRepository;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.user.model.UserEntity;

import java.util.List;

@Component
public class UserRepositoryImpl extends AbstractDataRepository<UserEntity> implements UserRepository {

    public UserRepositoryImpl(List<UserEntity> dataStorage) {
        super(dataStorage);
    }

    @Override
    protected void validation(UserEntity userEntity) {
        boolean isValid = dataStorage.stream()
                .filter(user -> user.getId() != userEntity.getId())
                .noneMatch(user -> user.getEmail().equals(userEntity.getEmail()));
        if (!isValid) {
            throw new AlreadyExistException(String.format(ErrorMessage.EMAIL_ALREADY_EXIST, userEntity.getEmail()));
        }
    }
}