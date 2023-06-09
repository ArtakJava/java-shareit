package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingState;
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
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    public static final Sort SORT_BY_START_DESC = Sort.by("start").descending();
    public static final Sort SORT_BY_START_ASC = Sort.by("start").ascending();
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;

    @Override
    public ItemDtoWithBooking create(long userId, ItemDtoWithOutBooking itemDto) {
        Item item = ItemMapper.mapToItemEntity(itemDto);
        User user = getUser(userId);
        item.setUser(user);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            item.setRequest(requestRepository.getReferenceById(itemDto.getRequestId()));
        }
        ItemDtoWithBooking result = ItemMapper.mapToItemDto(itemRepository.save(item));
        log.info(MessageHolder.SUCCESS_CREATE, result);
        return result;
    }

    @Override
    public ItemDtoWithBooking get(long userId, long itemId) {
        User user = getUser(userId);
        Item item = itemRepository.getReferenceById(itemId);
        ItemDtoWithBooking itemDto;
        BookingDtoWithBooker lastBooking = null;
        BookingDtoWithBooker nextBooking = null;
        List<CommentDto> comments;
        if (item.getUser().getId() == user.getId()) {
            List<BookingDtoWithBooker> lastBookings = BookingMapper.mapBooksToBookingsDtoWithBooker(
                    bookingRepository.findByItemIdAndStateNotAndStartBefore(
                            itemId,
                            BookingState.REJECTED,
                            LocalDateTime.now(),
                            PageRequest.of(0, 1, SORT_BY_START_DESC)
                    )
            );
            List<BookingDtoWithBooker> nextBookings = BookingMapper.mapBooksToBookingsDtoWithBooker(
                    bookingRepository.findByItemIdAndStateNotAndStartAfter(
                            itemId,
                            BookingState.REJECTED,
                            LocalDateTime.now(),
                            PageRequest.of(0, 1, SORT_BY_START_ASC)
                    )
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
        log.info(MessageHolder.SUCCESS_GET, itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDtoWithBooking> getAllByUser(long userId) {
        User user = getUser(userId);
        List<ItemDtoWithBooking> itemsByUser = itemRepository.findAllByUserOrderById(user).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
        List<Comment> allComments = commentRepository.findByItemIdIn(
                itemsByUser.stream()
                        .map(ItemDtoWithBooking::getId)
                        .collect(Collectors.toSet()));
        Map<Long, List<Comment>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        List<BookingDtoWithBookerAndItem> nextBookingDtoWithBookers =
                bookingRepository.findNextBookingForItemsByUser(userId, LocalDateTime.now());
        log.info(MessageHolder.SUCCESS_GET_ALL_ITEMS_BY_USER, userId);
        Map<Long, BookingDtoWithBooker> nextBookingByItem = nextBookingDtoWithBookers.stream()
                .collect(Collectors.toMap(
                        BookingDtoWithBookerAndItem::getItemId,
                        dto -> new BookingDtoWithBooker(dto.getId(), dto.getBookerId()), (first, second) -> second));
        List<BookingDtoWithBookerAndItem> lastBookingDtoWithBookers =
                bookingRepository.findLastBookingForItemsByUser(userId, LocalDateTime.now());
        log.info(MessageHolder.SUCCESS_GET_ALL_ITEMS_BY_USER, userId);
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
        User user = getUser(userId);
        Item oldItem = itemRepository.getReferenceById(itemId);
        if (oldItem.getUser().getId() == user.getId()) {
            Item result = itemRepository.save(getUpdatedItem(oldItem, ItemMapper.mapToItemEntity(itemDtoPatch)));
            log.info(MessageHolder.SUCCESS_UPDATE, ItemMapper.mapToItemDto(result));
            return ItemMapper.mapToItemDto(result);
        } else {
            throw new NotValidOwnerForUpdateException(
                    String.format(MessageHolder.USER_ID_NOT_VALID, userId, itemId));
        }
    }

    @Transactional
    @Override
    public void delete(long userId, long itemId) {
        User user = getUser(userId);
        if (itemRepository.getReferenceById(itemId).getUser().getId() == user.getId()) {
            itemRepository.delete(itemRepository.getReferenceById(itemId));
            log.info(MessageHolder.SUCCESS_DELETE, itemId);
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
        log.info(MessageHolder.SUCCESS_SEARCH_ITEMS, text);
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
            User user = getUser(authorId);
            Item item = itemRepository.getReferenceById(itemId);
            Comment comment = CommentMapper.mapToCommentEntity(commentDto, user, item);
            CommentDto result = CommentMapper.mapToCommentDto(commentRepository.save(comment));
            log.info(MessageHolder.GET_UPDATE_REQUEST, itemId);
            return result;
        } else {
            throw new UnBookingCommentException(String.format(MessageHolder.AUTHOR_NOT_BOOKING, authorId, itemId));
        }
    }

    @Override
    public User getUser(long userId) {
        User result = new User();
        User user = userRepository.getReferenceById(userId);
        if (user.getName() != null) {
            result = user;
        }
        return result;
    }
}