package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotValidDataForUpdateException;
import ru.practicum.shareit.exception.NotValidOwnerException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;
import ru.practicum.shareit.CustomJsonPatch;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final ItemMapper mapper;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        ItemEntity itemEntity = mapper.mapToItemEntity(itemDto);
        itemEntity.setOwnerId(userId);
        ItemDto result = mapper.mapToItemDto(repository.create(itemEntity));
        log.info(InfoMessage.SUCCESS_CREATE, result);
        return result;
    }

    @Override
    public ItemDto get(long userId, long itemId) {
        ItemDto itemDto = mapper.mapToItemDto(repository.get(itemId));
        log.info(InfoMessage.SUCCESS_GET, itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByUser(long userId) {
        List<ItemDto> itemsByUser = repository.getAllByUser(userId).stream()
                .map(mapper::mapToItemDto)
                .collect(Collectors.toList());
        log.info(InfoMessage.SUCCESS_GET_ALL_ITEMS_BY_USER, userId);
        return itemsByUser;
    }

    @Override
    public ItemDto update(long userId, long itemId, CustomJsonPatch patch) {
        ItemDto oldItemDto = mapper.mapToItemDto(repository.get(itemId));
        if (oldItemDto.getOwnerId() == userId) {
            ItemDto itemDtoPatched = applyPatchToItem(patch, oldItemDto);
            ItemEntity result = repository.update(
                    mapper.mapToItemEntity(oldItemDto), mapper.mapToItemEntity(itemDtoPatched));
            log.info(InfoMessage.SUCCESS_UPDATE, itemDtoPatched);
            return mapper.mapToItemDto(result);
        } else {
            throw new NotValidOwnerException(
                    String.format(ErrorMessage.USER_ID_NOT_VALID, userId, itemId));
        }
    }

    @Override
    public void delete(long userId, long itemId) {
        if (repository.get(itemId).getOwnerId() == userId) {
            repository.delete(itemId);
            log.info(InfoMessage.SUCCESS_DELETE, itemId);
        }
    }

    @Override
    public List<ItemDto> search(String text) {
        List<ItemDto> items = repository.search(text).stream()
                .map(mapper::mapToItemDto)
                .collect(Collectors.toList());
        log.info(InfoMessage.SUCCESS_SEARCH_ITEMS, text);
        return items;
    }

    @Override
    public ItemDto applyPatchToItem(CustomJsonPatch patch, ItemDto oldItemDto) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode patched = patch.apply(objectMapper.convertValue(oldItemDto, JsonNode.class));
            return objectMapper.treeToValue(patched, ItemDto.class);
        } catch (JsonPatchException | IOException e) {
            throw new NotValidDataForUpdateException(
                    String.format(ErrorMessage.DATA_NOT_VALID_FOR_UPDATE, patch, oldItemDto));
        }
    }
}