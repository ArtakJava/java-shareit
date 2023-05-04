package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.AbstractDataRepository;
import ru.practicum.shareit.exception.NotValidOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.ErrorMessage;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemRepositoryImpl extends AbstractDataRepository<Item> implements ItemRepository {
    private final Map<Long, Map<Long, Item>> userItems;

    public ItemRepositoryImpl(Map<Long, Item> dataStorage,
                              Map<Long, Map<Long, Item>> userItems) {
        super(dataStorage);
        this.userItems = userItems;
    }

    @Override
    public Item create(Item item) {
        userItems.compute(item.getOwner().getId(), (owner, items) -> {
            if (!userItems.containsKey(owner)) {
                items = new HashMap<>();
            }
            items.put(item.getId(), item);
            return items;
        });
        return super.create(item);
    }

    @Override
    public List<Item> getAllByUser(long userId) {
        return new ArrayList<>(userItems.get(userId).values());
    }

    @Override
    public List<Item> search(String text) {
        List<Item> items = new ArrayList<>();
        if (!text.isBlank()) {
            items = getAll().stream()
                    .filter(itemEntity -> itemEntity.getAvailable()
                            && itemEntity.getDescription().toLowerCase().contains(text.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return items;
    }

    @Override
    public void validation(Item dataEntity) {
        if (dataEntity.getOwner() != null) {
            boolean isValid = userItems.containsKey(dataEntity.getOwner().getId());
            if (!isValid) {
                throw new NotValidOwnerException(
                        String.format(ErrorMessage.OWNER_ID_NOT_FOUND, dataEntity.getOwner().getId(), dataEntity.getId()));
            }
        }
    }
}