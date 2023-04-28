package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.AbstractDataRepository;
import ru.practicum.shareit.exception.NotValidOwnerException;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRepositoryImpl extends AbstractDataRepository<ItemEntity> implements ItemRepository {
    private final UserRepository userRepository;

    public ItemRepositoryImpl(List<ItemEntity> dataStorage, UserRepository userRepository) {
        super(dataStorage);
        this.userRepository = userRepository;
    }

    @Override
    public List<ItemEntity> getAllByUser(long userId) {
        return getAll().stream()
                .filter(itemEntity -> itemEntity.getOwnerId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemEntity> search(String text) {
        List<ItemEntity> items = new ArrayList<>();
        if (!text.isBlank()) {
            items = getAll().stream()
                    .filter(itemEntity -> itemEntity.getDescription().toLowerCase().contains(text.toLowerCase())
                            && itemEntity.isAvailable())
                    .collect(Collectors.toList());
        }
        return items;
    }

    @Override
    protected void validation(ItemEntity dataEntity) {
        super.validation(dataEntity);
        boolean isValid = userRepository.getAll().stream()
                .anyMatch(userEntity -> userEntity.getId() == dataEntity.getOwnerId());
        if (!isValid) {
            throw new NotValidOwnerException(
                    String.format(ErrorMessage.OWNER_ID_NOT_FOUND, dataEntity.getOwnerId(), dataEntity.getId()));
        }
    }
}