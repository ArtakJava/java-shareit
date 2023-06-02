package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {
    private final BookingService service;
    private final EntityManager em;
    private User user;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private LocalDateTime start;
    private LocalDateTime end;
    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        String str = "2023-06-05 11:30:40";
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        user = makeUserEntity("Ivan", "ivan@email");
        em.persist(user);
        em.flush();
        UserDto userDto = makeUserDto("Ivan", "ivan@email");
        booker = makeUserEntity("Dima", "dima@email");
        em.persist(booker);
        em.flush();
        UserDto bookerDto = makeUserDto("Dima", "ivan@dima");

        item = makeItemEntity("item N1", "description", true, user);
        em.persist(item);
        em.flush();
        ItemDtoWithOutBooking itemDto = makeItemDto("item N1", "description", true);
        ItemDtoWithBooking itemDtoWithBooking = makeItemDtoWithBooking("item N1", "description", true, user.getId());
        ItemDtoWithOutBooking itemDtoPatch = makeItemDtoPatch("item N1 update");
        Item itemPatched = makeItemEntity("item N1 update", "description", true, user);

        String startInstr = "2023-06-05 11:30:40";
        String endInstr = "2023-06-05 11:50:40";
        start = LocalDateTime.parse(startInstr, formatter);
        end = LocalDateTime.parse(endInstr, formatter);
        booking = makeBookingEntity(start, end, item, booker);
        bookingDto = makeBookingDto(start, end, item.getId());
        BookingDtoWithInfo bookingDtoWithInfo = makeBookingDtoWithInfo(start, end, itemDtoWithBooking, bookerDto, BookingState.WAITING);
    }

    @Test
    void testCreate() {
        BookingDtoWithInfo result = service.create(booker.getId(), bookingDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(UserMapper.mapToUserDto(booker)));
        assertThat(result.getItem(), equalTo(ItemMapper.mapToItemDto(item)));
    }

    @Test
    void testCreateDB() {
        service.create(booker.getId(), bookingDto);
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.start = :start", Booking.class);
        Booking result = query.setParameter("start", bookingDto.getStart()).getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(booker));
        assertThat(result.getItem(), equalTo(item));
    }

    @Test
    void testGet() {
        em.persist(booking);
        em.flush();
        BookingDtoWithInfo result = service.get(booker.getId(), booking.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(UserMapper.mapToUserDto(booker)));
        assertThat(result.getItem(), equalTo(ItemMapper.mapToItemDto(item)));
    }

    @Test
    void testAllByBooker() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByBooker(booker.getId(),
                new Filter(new StateHolder("ALL"), new PageParameter(null, null)));
        assertThat(bookings, hasSize(sourceBookings.size()));
        for (Booking booking: sourceBookings) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd())),
                    hasProperty("booker", equalTo(UserMapper.mapToUserDto(booking.getBooker()))),
                    hasProperty("item", equalTo(ItemMapper.mapToItemDto(booking.getItem()))),
                    hasProperty("status", equalTo(booking.getState()))
            )));
        }
    }

    @Test
    void testGetAllByOwner() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByOwner(user.getId(),
                new Filter(new StateHolder("ALL"), new PageParameter(null, null)));
        assertThat(bookings, hasSize(sourceBookings.size()));
        for (Booking booking: sourceBookings) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(booking.getStart())),
                    hasProperty("end", equalTo(booking.getEnd())),
                    hasProperty("booker", equalTo(UserMapper.mapToUserDto(booking.getBooker()))),
                    hasProperty("item", equalTo(ItemMapper.mapToItemDto(booking.getItem()))),
                    hasProperty("status", equalTo(booking.getState()))
            )));
        }
    }

    private User makeUserEntity(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }

    private Request makeRequestEntity(String description, LocalDateTime created, User requestor) {
        Request request = new Request();
        request.setDescription(description);
        request.setCreated(created);
        request.setRequestor(requestor);
        return request;
    }

    private Item makeItemEntity(String name, String description, Boolean available, User user) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setUser(user);
        return item;
    }

    private ItemDtoWithOutBooking makeItemDto(String name, String description, Boolean available) {
        ItemDtoWithOutBooking itemDto = new ItemDtoWithOutBooking();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }

    private ItemDtoWithBooking makeItemDtoWithBooking(String name, String description, Boolean available, long requestId) {
        ItemDtoWithBooking itemDtoWithBooking = new ItemDtoWithBooking();
        itemDtoWithBooking.setName(name);
        itemDtoWithBooking.setDescription(description);
        itemDtoWithBooking.setAvailable(available);
        itemDtoWithBooking.setRequestId(requestId);
        return itemDtoWithBooking;
    }

    private ItemDtoWithOutBooking makeItemDtoPatch(String name) {
        ItemDtoWithOutBooking itemDto = new ItemDtoWithOutBooking();
        itemDto.setName(name);
        return itemDto;
    }



    private Booking makeBookingEntity(LocalDateTime dateTimeOne,
                                       LocalDateTime dateTimeTwo,
                                       Item item,
                                       User user) {
        Booking booking = new Booking();
        booking.setStart(dateTimeOne);
        booking.setEnd(dateTimeTwo);
        booking.setItem(item);
        booking.setBooker(user);
        return booking;
    }

    private BookingDto makeBookingDto(LocalDateTime dateTimeOne,
                                      LocalDateTime dateTimeTwo,
                                      long itemId) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(dateTimeOne);
        bookingDto.setEnd(dateTimeTwo);
        bookingDto.setItemId(itemId);
        return bookingDto;
    }

    private BookingDtoWithInfo makeBookingDtoWithInfo(LocalDateTime dateTimeOne,
                                                      LocalDateTime dateTimeTwo,
                                                      ItemDtoWithBooking item,
                                                      UserDto user,
                                                      BookingState state) {
        BookingDtoWithInfo bookingDto = new BookingDtoWithInfo();
        bookingDto.setStart(dateTimeOne);
        bookingDto.setEnd(dateTimeTwo);
        bookingDto.setItem(item);
        bookingDto.setBooker(user);
        bookingDto.setStatus(state);
        return bookingDto;
    }

    private CommentDto makeCommentDto(String text, String authorName, LocalDateTime dateTimeOne) {
        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        commentDto.setAuthorName(authorName);
        commentDto.setCreated(dateTimeOne.toString());
        return commentDto;
    }
}