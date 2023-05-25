package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select new ru.practicum.shareit.booking.model.Booking(b.id, b.start, b.end, b.item, b.booker, b.state) " +
            "from Booking as b where b.item.user.id = ?1 and b.state in (?2) order by b.start desc")
    List<Booking> findByOwnerIdAndStateIn(long ownerId, Set<BookingState> states);

    @Query("select new ru.practicum.shareit.booking.model.Booking(b.id, b.start, b.end, b.item, b.booker, b.state) " +
            "from Booking as b where b.booker.id = ?1 and b.state in (?2) order by b.start desc")
    List<Booking> findByBookerIdAndStateIn(long bookerId, Set<BookingState> states);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId, LocalDateTime now, LocalDateTime now1);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.model.Booking(b.id, b.start, b.end, b.item, b.booker, b.state) " +
            "from Booking as b where b.item.user.id = ?1 and b.end < ?2 order by b.start desc")
    List<Booking> findByOwnerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.model.Booking(b.id, b.start, b.end, b.item, b.booker, b.state) " +
            "from Booking as b where b.item.user.id = ?1 and b.start < ?2 and b.end > ?2 order by b.start desc")
    List<Booking> findByOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.model.Booking(b.id, b.start, b.end, b.item, b.booker, b.state) " +
            "from Booking as b where b.item.user.id = ?1 and b.start > ?2 order by b.start desc")
    List<Booking> findByOwnerIdAndStartAfterOrderByStartDesc(long ownerId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoWithBooker(b.id, b.booker.id) " +
            "from Booking as b where b.item.id = ?1 and b.state not like 'REJECTED' and b.end < ?2 order by b.start desc")
    List<BookingDtoWithBooker> findLastBookingForItem(long itemId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoWithBooker(b.id, b.booker.id) " +
            "from Booking as b where b.item.id = ?1 and b.state not like 'REJECTED' and b.start > ?2 order by b.start asc")
    List<BookingDtoWithBooker> findNextBookingForItem(long itemId, LocalDateTime now);

    List<Booking> findByBookerIdAndItemIdAndEndBefore(long authorId, long itemId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem(b.id, b.booker.id, b.item.id) " +
            "from Booking as b where b.item.user.id = ?1 and b.state not like 'REJECTED' and b.start > ?2 order by b.start desc")
    List<BookingDtoWithBookerAndItem> findNextBookingForItemsByUser(long userId, LocalDateTime now);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem(b.id, b.booker.id, b.item.id) " +
            "from Booking as b where b.item.user.id = ?1 and b.state not like 'REJECTED' and b.end < ?2 order by b.start asc")
    List<BookingDtoWithBookerAndItem> findLastBookingForItemsByUser(long userId, LocalDateTime now);
}