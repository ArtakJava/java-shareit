package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.AbstractDataRepository;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.user.model.User;

import java.util.Map;
import java.util.Set;

@Component
public class UserRepositoryImpl extends AbstractDataRepository<User> implements UserRepository {
    private final Set<String> emails;

    public UserRepositoryImpl(Map<Long, User> dataStorage, Set<String> emails) {
        super(dataStorage);
        this.emails = emails;
    }

    @Override
    public User create(User user) {
        User result = super.create(user);
        emails.add(user.getEmail());
        return result;
    }

    @Override
    public User update(User user, User userPatch, Class<User> tClass) throws NoSuchFieldException, IllegalAccessException {
        emails.remove(user.getEmail());
        User result = super.update(user, userPatch, tClass);
        emails.add(user.getEmail());
        return result;
    }

    @Override
    public void delete(long userId) {
        emails.remove(dataStorage.get(userId).getEmail());
        super.delete(userId);
    }

    @Override
    public void validation(User user) {
        boolean emailAlreadyExist = emails.contains(user.getEmail());
        if (emailAlreadyExist) {
            throw new AlreadyExistException(String.format(ErrorMessage.EMAIL_ALREADY_EXIST, user.getEmail()));
        }
    }
}