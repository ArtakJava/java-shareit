package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfo;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;
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
    private final BookingMapper mapper;

    @Override
    public BookingInfo create(long userId, BookingDto bookingDto) {
        dateIsValid(bookingDto);
        Item item = itemRepository.getReferenceById(bookingDto.getItemId());
        User booker = userRepository.getReferenceById(userId);
        Booking booking = mapper.mapToBookingEntity(bookingDto, item, booker);
        if (userId != booking.getItem().getUser().getId() && booker.getName() != null && item.getAvailable()) {
            BookingInfo result = mapper.mapToBookingInfo(bookingRepository.save(booking));
            log.info(InfoMessage.SUCCESS_CREATE, result);
            return result;
        } else if (userId != booking.getItem().getUser().getId() && !item.getAvailable()) {
            throw new NotAvailableItemException(
                    String.format(ErrorMessage.AVAILABLE_NOT_AVAILABLE, item.getId())
            );
        } else {
            throw new NotFoundException(
                    String.format(ErrorMessage.OWNER_ITEM, userId)
            );
        }
    }

    @Override
    public BookingInfo get(long userId, long bookingId) {
        Booking booking = bookingRepository.getReferenceById(bookingId);
        if (userId == booking.getBooker().getId() || userId == booking.getItem().getUser().getId()) {
            log.info(InfoMessage.SUCCESS_GET, userId);
            return mapper.mapToBookingInfo(booking);
        } else {
            throw new NotValidOwnerException(
                    String.format(ErrorMessage.BOOKER_OR_OWNER_ID_NOT_VALID, userId, bookingId)
            );
        }
    }

    @Override
    public List<BookingInfo> getAllByBooker(long bookerId, StateHolder stateHolder) {
        Set<BookingState> states = new HashSet<>();
        User user = userRepository.getReferenceById(bookerId);
        if (user.getName() == null) {
            throw new NotFoundException(String.format(ErrorMessage.USER_ID_NOT_FOUND, bookerId));
        }
        List<Booking> bookings = new ArrayList<>();
        List<BookingInfo> result;
        BookingState bookingState = stateHolder.getState();
        if (bookingState instanceof BookingApproveState) {
            states.add(bookingState);
        } else if (BookingTempState.ALL.equals(bookingState)) {
            states.addAll(List.of(BookingApproveState.values()));
        } else {
            if (BookingTempState.PAST.equals(bookingState)) {
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                        bookerId,
                        LocalDateTime.now()
                );
            } else if (BookingTempState.CURRENT.equals(bookingState)) {
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );
            } else if (BookingTempState.FUTURE.equals(bookingState)) {
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                        bookerId,
                        LocalDateTime.now()
                );
            }
            result = bookings.stream()
                    .map(mapper::mapToBookingInfo)
                    .collect(Collectors.toList());
            return result;
        }
        result = bookingRepository.findByBookerIdAndStateIn(bookerId, states).stream()
                .map(mapper::mapToBookingInfo)
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<BookingInfo> getAllByOwner(long ownerId, StateHolder stateHolder) {
        Set<BookingState> states = new HashSet<>();
        User user = userRepository.getReferenceById(ownerId);
        if (user.getName() == null) {
            throw new NotFoundException(String.format(ErrorMessage.USER_ID_NOT_FOUND, ownerId));
        }
        List<Booking> bookings = new ArrayList<>();
        List<BookingInfo> result;
        BookingState bookingState = stateHolder.getState();
        if (bookingState instanceof BookingApproveState) {
            states.add(bookingState);
        } else if (BookingTempState.ALL.equals(bookingState)) {
            states.addAll(List.of(BookingApproveState.values()));
        } else {
            if (BookingTempState.PAST.equals(bookingState)) {
                bookings = bookingRepository.findByOwnerIdAndEndBeforeOrderByStartDesc(
                        ownerId,
                        LocalDateTime.now()
                );
            } else if (BookingTempState.CURRENT.equals(bookingState)) {
                bookings = bookingRepository.findByOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        ownerId,
                        LocalDateTime.now()
                );
            } else if (BookingTempState.FUTURE.equals(bookingState)) {
                bookings = bookingRepository.findByOwnerIdAndStartAfterOrderByStartDesc(
                        ownerId,
                        LocalDateTime.now()
                );
            }
            result = bookings.stream()
                    .map(mapper::mapToBookingInfo)
                    .collect(Collectors.toList());
            return result;
        }
        result = bookingRepository.findByOwnerIdAndStateIn(ownerId, states).stream()
                .map(mapper::mapToBookingInfo)
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public BookingInfo approve(long userId, long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.getReferenceById(bookingId);
        if (!booking.getState().equals(BookingApproveState.APPROVED)) {
            if (userId == booking.getItem().getUser().getId()) {
                if (isApproved) {
                    booking.setState(BookingApproveState.APPROVED);
                } else {
                    booking.setState(BookingApproveState.REJECTED);
                }
                BookingInfo result = mapper.mapToBookingInfo(bookingRepository.save(booking));
                log.info(InfoMessage.SUCCESS_CREATE, result);
                return result;
            } else {
                throw new NotValidOwnerException(
                        String.format(ErrorMessage.USER_ID_NOT_VALID, userId, booking.getItem().getId())
                );
            }
        } else {
            throw new NotValidDataForUpdateException(String.format(ErrorMessage.BOOKING_ALREADY_APPROVED, bookingId));
        }
    }

    @Override
    public void delete(long userId) {
        bookingRepository.delete(bookingRepository.getReferenceById(userId));
        log.info(InfoMessage.SUCCESS_DELETE, userId);
    }

    public boolean dateIsValid(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (start == null) {
            throw new NotValidDateException(ErrorMessage.START_IS_NUll);
        }
        if (end == null) {
            throw new NotValidDateException(ErrorMessage.END_IS_NUll);
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new NotValidDateException(ErrorMessage.START_IN_PAST);
        }
        if (end.isBefore(LocalDateTime.now())) {
            throw new NotValidDateException(ErrorMessage.END_IN_PAST);
        }
        if (end.isBefore(start)) {
            throw new NotValidDateException(ErrorMessage.END_BEFORE_START);
        } else if (start.isBefore(end)) {
            return true;
        } else {
            throw new NotValidDateException(ErrorMessage.START_EQUAL_END);
        }
    }
}