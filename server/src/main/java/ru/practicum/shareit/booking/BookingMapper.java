package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper implements Serializable {

    public static BookingDtoWithInfo mapToBookingInfo(Booking booking) {
        return BookingDtoWithInfo.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemMapper.mapToItemDto(booking.getItem()))
                .booker(UserMapper.mapToUserDto(booking.getBooker()))
                .status(booking.getState())
                .build();
    }

    public static Booking mapToBookingEntity(BookingDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .state(bookingDto.getStatus())
                .build();
    }

    public static List<BookingDtoWithBooker> mapBooksToBookingsDtoWithBooker(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> new BookingDtoWithBooker(booking.getId(), booking.getBooker().getId()))
                .collect(Collectors.toList());
    }
}