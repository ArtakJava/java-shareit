package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.dao.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.NotValidOwnerForUpdateException;
import ru.practicum.shareit.exception.UnBookingCommentException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDtoWithBooking create(long userId, ItemDtoWithOutBooking itemDto) {
        Item item = ItemMapper.mapToItemEntity(itemDto);
        User user = userRepository.getReferenceById(userId);
        if (user.getName() != null) {
            item.setUser(user);
            ItemDtoWithBooking result = ItemMapper.mapToItemDto(itemRepository.save(item));
            log.info(InfoMessage.SUCCESS_CREATE, result);
            return result;
        } else {
            throw new EntityNotFoundException(String.format(ErrorMessage.OWNER_ID_NOT_FOUND_FOR_ITEM, user, item.getId()));
        }
    }

    @Override
    public ItemDtoWithBooking get(long userId, long itemId) {
        Item item = itemRepository.getReferenceById(itemId);
        ItemDtoWithBooking itemDto;
        BookingDtoWithBooker lastBooking = null;
        BookingDtoWithBooker nextBooking = null;
        List<CommentDto> comments;
        if (userId == item.getUser().getId()) {
            List<BookingDtoWithBooker> lastBookings = bookingRepository.findLastBookingForItem(
                    itemId,
                    LocalDateTime.now()
            );
            List<BookingDtoWithBooker> nextBookings = bookingRepository.findNextBookingForItem(
                    itemId,
                    LocalDateTime.now()
            );
            if (!lastBookings.isEmpty()) {
                lastBooking = lastBookings.get(0);
            }
            if (!nextBookings.isEmpty()) {
                nextBooking = nextBookings.get(0);
            }
        }
        comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
        itemDto = ItemMapper.mapToItemDtoWithBookingsAndComments(item, lastBooking, nextBooking, comments);
        log.info(InfoMessage.SUCCESS_GET, itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDtoWithBooking> getAllByUser(long userId) {
        List<ItemDtoWithBooking> itemsByUser = itemRepository.findAllByUserOrderById(userRepository.getReferenceById(userId)).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());

        List<Comment> allComments = commentRepository.findByItemIdIn(
                itemsByUser.stream()
                        .map(ItemDtoWithBooking::getId)
                        .collect(Collectors.toSet()));
        Map<Long, List<Comment>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        List<BookingDtoWithBookerAndItem> nextBookingDtoWithBookers = bookingRepository.findNextBookingForItemsByUser(userId, LocalDateTime.now());
        log.info(InfoMessage.SUCCESS_GET_ALL_ITEMS_BY_USER, userId);
        Map<Long, BookingDtoWithBooker> nextBookingByItem = nextBookingDtoWithBookers.stream()
                .collect(Collectors.toMap(
                        BookingDtoWithBookerAndItem::getItemId,
                        dto -> new BookingDtoWithBooker(dto.getId(), dto.getBookerId()), (first, second) -> second));
        List<BookingDtoWithBookerAndItem> lastBookingDtoWithBookers = bookingRepository.findLastBookingForItemsByUser(userId, LocalDateTime.now());
        log.info(InfoMessage.SUCCESS_GET_ALL_ITEMS_BY_USER, userId);
        Map<Long, BookingDtoWithBooker> lastBookingByItem = lastBookingDtoWithBookers.stream()
                .collect(Collectors.toMap(
                        BookingDtoWithBookerAndItem::getItemId,
                        dto -> new BookingDtoWithBooker(dto.getId(), dto.getBookerId()), (first, second) -> second));
        return itemsByUser.stream()
                .map(itemDto -> ItemMapper.mapToItemDtoWithBookingsAndComments(
                        itemDto,
                        lastBookingByItem.getOrDefault(itemDto.getId(), null),
                        nextBookingByItem.getOrDefault(itemDto.getId(), null),
                        commentsByItem.getOrDefault(itemDto.getId(), new ArrayList<>()).stream()
                                .map(CommentMapper::mapToCommentDto)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDtoWithBooking update(long userId, long itemId, ItemDtoWithOutBooking itemDtoPatch) {
        Item oldItem = itemRepository.getReferenceById(itemId);
        if (oldItem.getUser().getId() == userId) {
            Item result = itemRepository.save(getUpdatedItem(oldItem, ItemMapper.mapToItemEntity(itemDtoPatch)));
            log.info(InfoMessage.SUCCESS_UPDATE, ItemMapper.mapToItemDto(result));
            return ItemMapper.mapToItemDto(result);
        } else {
            throw new NotValidOwnerForUpdateException(
                    String.format(ErrorMessage.USER_ID_NOT_VALID, userId, itemId));
        }
    }

    @Transactional
    @Override
    public void delete(long userId, long itemId) {
        if (itemRepository.getReferenceById(itemId).getUser().getId() == userId) {
            itemRepository.delete(itemRepository.getReferenceById(itemId));
            log.info(InfoMessage.SUCCESS_DELETE, itemId);
        }
    }

    @Override
    public List<ItemDtoWithBooking> search(String text) {
        List<ItemDtoWithBooking> items = new ArrayList<>();
        if (!text.isBlank()) {
            items = itemRepository
                    .findByNameContainingIgnoreCaseAndAvailableTrueOrDescriptionContainingIgnoreCaseAndAvailableTrue(text, text)
                    .stream()
                    .map(ItemMapper::mapToItemDto)
                    .collect(Collectors.toList());
        }
        log.info(InfoMessage.SUCCESS_SEARCH_ITEMS, text);
        return items;
    }

    @Override
    public Item getUpdatedItem(Item item, Item itemPatch) {
        if (itemPatch.getName() != null) {
            item.setName(itemPatch.getName());
        }
        if (itemPatch.getDescription() != null) {
            item.setDescription(itemPatch.getDescription());
        }
        if (itemPatch.getAvailable() != null) {
            item.setAvailable(itemPatch.getAvailable());
        }
        return item;
    }

    @Override
    public CommentDto createComment(long authorId, long itemId, CommentDto commentDto) {
        List<Booking> booking = bookingRepository.findByBookerIdAndItemIdAndEndBefore(authorId, itemId, LocalDateTime.now());
        if (!booking.isEmpty()) {
            User user = userRepository.getReferenceById(authorId);
            Item item = itemRepository.getReferenceById(itemId);
            Comment comment = CommentMapper.mapToCommentEntity(commentDto, user, item);
            CommentDto result = CommentMapper.mapToCommentDto(commentRepository.save(comment));
            log.info(InfoMessage.GET_UPDATE_REQUEST, itemId);
            return result;
        } else {
            throw new UnBookingCommentException(String.format(ErrorMessage.AUTHOR_NOT_BOOKING, authorId, itemId));
        }
    }
}