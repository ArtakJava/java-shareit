package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotValidOwnerException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        Item item = ItemMapper.mapToItemEntity(itemDto);
        item.setOwner(userRepository.get(userId));
        ItemDto result = ItemMapper.mapToItemDto(repository.create(item));
        log.info(InfoMessage.SUCCESS_CREATE, result);
        return result;
    }

    @Override
    public ItemDto get(long userId, long itemId) {
        ItemDto itemDto = ItemMapper.mapToItemDto(repository.get(itemId));
        log.info(InfoMessage.SUCCESS_GET, itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByUser(long userId) {
        List<ItemDto> itemsByUser = repository.getAllByUser(userId).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
        log.info(InfoMessage.SUCCESS_GET_ALL_ITEMS_BY_USER, userId);
        return itemsByUser;
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDtoPatch) throws NoSuchFieldException, IllegalAccessException {
        Item oldItem = repository.get(itemId);
        if (oldItem.getOwner().getId() == userId) {
            Item result = repository.update(oldItem, ItemMapper.mapToItemEntity(itemDtoPatch), Item.class);
            log.info(InfoMessage.SUCCESS_UPDATE, ItemMapper.mapToItemDto(result));
            return ItemMapper.mapToItemDto(result);
        } else {
            throw new NotValidOwnerException(
                    String.format(ErrorMessage.USER_ID_NOT_VALID, userId, itemId));
        }
    }

    @Override
    public void delete(long userId, long itemId) {
        if (repository.get(itemId).getOwner().getId() == userId) {
            repository.delete(itemId);
            log.info(InfoMessage.SUCCESS_DELETE, itemId);
        }
    }

    @Override
    public List<ItemDto> search(String text) {
        List<ItemDto> items = repository.search(text).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
        log.info(InfoMessage.SUCCESS_SEARCH_ITEMS, text);
        return items;
    }
}