package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemUserIdAndStateIn(long ownerId, Set<BookingState> states, PageRequest pageRequest);

    List<Booking> findByItemUserIdAndStateIn(long ownerId, Set<BookingState> states, Sort sort);

    List<Booking> findByBookerIdAndStateIn(long bookerId, Set<BookingState> states, PageRequest pageRequest);

    List<Booking> findByBookerIdAndStateIn(long bookerId, Set<BookingState> states, Sort sort);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            long bookerId,
            LocalDateTime now,
            LocalDateTime nowRepeat
    );

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now);

    List<Booking> findByItemUserIdAndEndBeforeOrderByStartDesc(long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemUserIdAndStartBeforeAndEndAfterOrderByStartDesc(
            long ownerId,
            LocalDateTime now,
            LocalDateTime nowRepeat,
            Sort sort);

    List<Booking> findByItemUserIdAndStartAfterOrderByStartDesc(long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemIdAndStateNotAndStartBefore(
            long itemId,
            BookingState state,
            LocalDateTime now,
            Sort sort
    );

    List<Booking> findByItemIdAndStateNotAndStartAfter(
            long itemId,
            BookingState state,
            LocalDateTime now,
            Sort sort
    );

    List<Booking> findByBookerIdAndItemIdAndEndBefore(long authorId, long itemId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem(b.id, b.booker.id, b.item.id) " +
            "from Booking as b " +
            "where b.item.user.id = ?1 and b.state not like 'REJECTED' and b.start > ?2 " +
            "order by b.start desc")
    List<BookingDtoWithBookerAndItem> findNextBookingForItemsByUser(long userId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem(b.id, b.booker.id, b.item.id) " +
            "from Booking as b " +
            "where b.item.user.id = ?1 and b.state not like 'REJECTED' and b.end < ?2 " +
            "order by b.start asc")
    List<BookingDtoWithBookerAndItem> findLastBookingForItemsByUser(long userId, LocalDateTime now);
}