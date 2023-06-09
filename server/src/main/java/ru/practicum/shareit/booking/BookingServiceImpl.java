package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDtoWithInfo create(long userId, BookingDto bookingDto) {
        dateIsValid(bookingDto);
        BookingDtoWithInfo result;
        Item item = itemRepository.getReferenceById(bookingDto.getItemId());
        User booker = getUser(userId);
        Booking booking = BookingMapper.mapToBookingEntity(bookingDto, item, booker);
        if (booker.getId() != booking.getItem().getUser().getId() && item.getAvailable()) {
            result = BookingMapper.mapToBookingInfo(bookingRepository.save(booking));
            log.info(MessageHolder.SUCCESS_CREATE, result);
        } else if (booker.getId() != booking.getItem().getUser().getId() && !item.getAvailable()) {
            throw new NotAvailableItemException(
                    String.format(MessageHolder.AVAILABLE_NOT_AVAILABLE, item.getId())
            );
        } else {
            throw new NotFoundException(
                    String.format(MessageHolder.OWNER_ITEM, userId)
            );
        }
        return result;
    }

    @Override
    public BookingDtoWithInfo get(long userId, long bookingId) {
        User user = getUser(userId);
        Booking booking = bookingRepository.getReferenceById(bookingId);
        if (user.getId() == booking.getBooker().getId() || userId == booking.getItem().getUser().getId()) {
            log.info(MessageHolder.SUCCESS_GET, userId);
            return BookingMapper.mapToBookingInfo(booking);
        } else {
            throw new NotValidOwnerException(
                    String.format(MessageHolder.BOOKER_OR_OWNER_ID_NOT_VALID, userId, booking.getItem().getId())
            );
        }
    }

    @Override
    public List<BookingDtoWithInfo> getAllByBooker(long bookerId, Filter filter) {
        User booker = getUser(bookerId);
        Set<BookingState> states = new HashSet<>();
        List<Booking> bookings = new ArrayList<>();
        List<BookingDtoWithInfo> result;
        BookingState bookingState = filter.getStateHolder().getState();
        switch (bookingState) {
            case ALL:
                states.addAll(List.of(BookingState.values()));
                bookings = bookingRepository.findByBookerIdAndStateIn(
                        booker.getId(),
                        states,
                        filter.getPageRequest()
                );
                break;
            case APPROVED:
            case REJECTED:
            case WAITING:
                states.add(bookingState);
                bookings = bookingRepository.findByBookerIdAndStateIn(
                        booker.getId(),
                        states,
                        filter.getPageRequest()
                );
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                        booker.getId(),
                        LocalDateTime.now()
                );
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        booker.getId(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                        booker.getId(),
                        LocalDateTime.now()
                );
                break;
        }
        result = bookings.stream()
                .map(BookingMapper::mapToBookingInfo)
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<BookingDtoWithInfo> getAllByOwner(long ownerId, Filter filter) {
        User owner = getUser(ownerId);
        Set<BookingState> states = new HashSet<>();
        List<Booking> bookings = new ArrayList<>();
        List<BookingDtoWithInfo> result;
        BookingState bookingState = filter.getStateHolder().getState();
        switch (bookingState) {
            case ALL:
                states.addAll(List.of(BookingState.values()));
                bookings = bookingRepository.findByItemUserIdAndStateIn(
                        owner.getId(),
                        states,
                        filter.getPageRequest()
                );
                break;
            case APPROVED:
            case REJECTED:
            case WAITING:
                states.add(bookingState);
                bookings = bookingRepository.findByItemUserIdAndStateIn(
                        owner.getId(),
                        states,
                        filter.getPageRequest()
                );
                break;
            case PAST:
                bookings = bookingRepository.findByItemUserIdAndEndBeforeOrderByStartDesc(
                        owner.getId(),
                        LocalDateTime.now(),
                        filter.getPageRequest()
                );
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemUserIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        owner.getId(),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        filter.getPageRequest()
                );
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemUserIdAndStartAfterOrderByStartDesc(
                        owner.getId(),
                        LocalDateTime.now(),
                        filter.getPageRequest()
                );
                break;
            default:

        }
        result = bookings.stream()
                .map(BookingMapper::mapToBookingInfo)
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public BookingDtoWithInfo approve(long userId, long bookingId, boolean isApproved) {
        User user = getUser(userId);
        Booking booking = bookingRepository.getReferenceById(bookingId);
        if (booking.getState().equals(BookingState.WAITING)) {
            if (user.getId() == booking.getItem().getUser().getId()) {
                if (isApproved) {
                    booking.setState(BookingState.APPROVED);
                } else {
                    booking.setState(BookingState.REJECTED);
                }
                BookingDtoWithInfo result = BookingMapper.mapToBookingInfo(bookingRepository.save(booking));
                log.info(MessageHolder.SUCCESS_CREATE, result);
                return result;
            } else {
                throw new NotValidOwnerException(
                        String.format(MessageHolder.USER_ID_NOT_VALID, userId, booking.getItem().getId())
                );
            }
        } else {
            throw new NotValidDataForUpdateException(String.format(MessageHolder.BOOKING_ALREADY_APPROVED, bookingId));
        }
    }

    private void dateIsValid(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (start == null) {
            throw new NotValidDateException(MessageHolder.START_IS_NUll);
        }
        if (end == null) {
            throw new NotValidDateException(MessageHolder.END_IS_NUll);
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new NotValidDateException(MessageHolder.START_IN_PAST);
        }
        if (end.isBefore(LocalDateTime.now())) {
            throw new NotValidDateException(MessageHolder.END_IN_PAST);
        }
        if (end.isBefore(start)) {
            throw new NotValidDateException(MessageHolder.END_BEFORE_START);
        } else if (!start.isBefore(end)) {
            throw new NotValidDateException(MessageHolder.START_EQUAL_END);
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