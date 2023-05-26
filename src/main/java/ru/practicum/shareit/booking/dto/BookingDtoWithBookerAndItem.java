package ru.practicum.shareit.booking.dto;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class BookingDtoWithBookerAndItem extends BookingDtoWithBooker {
    private long itemId;

    public BookingDtoWithBookerAndItem(long bookingId, long bookerId, long itemId) {
        super(bookingId, bookerId);
        this.itemId = itemId;
    }
}