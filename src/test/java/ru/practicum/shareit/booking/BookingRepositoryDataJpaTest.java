package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoWithBookerAndItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingRepositoryDataJpaTest {
    private final TestEntityManager em;
    private final BookingRepository bookingRepository;
    private Booking lastBooking;
    private Booking nextBooking;
    private LocalDateTime start;
    private LocalDateTime end;
    private DateTimeFormatter formatter;
    private Item item;
    private User user;
    private User booker;

    @BeforeEach
    void setUp() {
        user = makeUserEntity("Ivan", "ivan@email");
        em.persist(user);
        booker = makeUserEntity("Dima", "dima@email");
        em.persist(booker);
        item = makeItemEntity("item N1", "description", true, user);
        em.persist(item);
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Test
    void nextBookingDtoWithBookers() {
        String startInstr = "2023-07-07 11:30:40";
        String endInstr = "2023-07-07 11:50:40";
        start = LocalDateTime.parse(startInstr, formatter);
        end = LocalDateTime.parse(endInstr, formatter);
        nextBooking = makeBookingEntity(start, end, item, booker, BookingState.WAITING);
        List<Booking> sourceBookings = new ArrayList<>(List.of(nextBooking));
        em.persist(nextBooking);
        List<BookingDtoWithBookerAndItem> nextBookingDtoWithBookers = bookingRepository.findNextBookingForItemsByUser(
                user.getId(), LocalDateTime.now()
        );
        assertThat(nextBookingDtoWithBookers, hasSize(sourceBookings.size()));
        for (Booking booking: sourceBookings) {
            assertThat(sourceBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    @Test
    void lastBookingDtoWithBookers() {
        String startInstr = "2023-05-07 11:30:40";
        String endInstr = "2023-05-07 11:50:40";
        start = LocalDateTime.parse(startInstr, formatter);
        end = LocalDateTime.parse(endInstr, formatter);
        lastBooking = makeBookingEntity(start, end, item, booker, BookingState.WAITING);
        List<Booking> sourceBookings = new ArrayList<>(List.of(lastBooking));
        em.persist(lastBooking);
        List<BookingDtoWithBookerAndItem> nextBookingDtoWithBookers = bookingRepository.findLastBookingForItemsByUser(
                user.getId(), LocalDateTime.now()
        );
        assertThat(nextBookingDtoWithBookers, hasSize(sourceBookings.size()));
        for (Booking booking: sourceBookings) {
            assertThat(sourceBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd()))
            )));
        }
    }

    private Booking makeBookingEntity(LocalDateTime dateTimeOne,
                                      LocalDateTime dateTimeTwo,
                                      Item item,
                                      User user,
                                      BookingState state) {
        Booking booking = new Booking();
        booking.setStart(dateTimeOne);
        booking.setEnd(dateTimeTwo);
        booking.setItem(item);
        booking.setState(state);
        booking.setBooker(user);
        return booking;
    }

    private Item makeItemEntity(String name, String description, Boolean available, User user) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setUser(user);
        return item;
    }

    private User makeUserEntity(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}