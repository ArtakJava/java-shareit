package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;

import java.util.List;

public interface BookingService {

    BookingDtoWithInfo create(long userId, BookingDto bookingDto);

    BookingDtoWithInfo get(long userId, long bookingId);

    List<BookingDtoWithInfo> getAllByBooker(long bookerId, StateHolder state);

    List<BookingDtoWithInfo> getAllByOwner(long userId, StateHolder state);

    BookingDtoWithInfo approve(long userId, long bookingId, boolean isApproved);

    void delete(long userId);
}