package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfo;

import java.util.List;

public interface BookingService {

    BookingInfo create(long userId, BookingDto bookingDto);

    BookingInfo get(long userId, long bookingId);

    List<BookingInfo> getAllByBooker(long bookerId, StateHolder state);

    List<BookingInfo> getAllByOwner(long userId, StateHolder state);

    BookingInfo approve(long userId, long bookingId, boolean isApproved);

    void delete(long userId);
}