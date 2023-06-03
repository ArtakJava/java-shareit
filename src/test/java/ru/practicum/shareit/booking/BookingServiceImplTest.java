package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.MessageHolder;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:test.properties")
public class BookingServiceImplTest {
    private final BookingService service;
    private final EntityManager em;
    private User user;
    private User otherUser;
    private User booker;
    private Item item;
    private Item itemTwo;
    private Booking booking;
    private BookingDto bookingDto;
    private LocalDateTime start;
    private LocalDateTime end;
    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        user = makeUserEntity("Ivan", "ivan@email");
        em.persist(user);
        em.flush();
        otherUser = makeUserEntity("Anna", "anna@email");
        em.persist(otherUser);
        em.flush();
        booker = makeUserEntity("Dima", "dima@email");
        em.persist(booker);
        em.flush();
        UserDto bookerDto = makeUserDto("Dima", "ivan@dima");

        item = makeItemEntity("item N1", "description", true, user);
        em.persist(item);
        em.flush();
        itemTwo = makeItemEntity("item N2", "description2", true, user);
        em.persist(itemTwo);
        em.flush();
        ItemDtoWithBooking itemDtoWithBooking = makeItemDtoWithBooking("item N1", "description", true, user.getId());
        String startInstr = "2023-06-07 11:30:40";
        String endInstr = "2023-06-07 11:50:40";
        start = LocalDateTime.parse(startInstr, formatter);
        end = LocalDateTime.parse(endInstr, formatter);
        booking = makeBookingEntity(start, end, item, booker, BookingState.WAITING);
        bookingDto = makeBookingDto(start, end, item.getId());
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
    void testCreateWithNotAvailableItem() {
        item.setAvailable(false);
        final NotAvailableItemException exception = assertThrows(
                NotAvailableItemException.class,
                () -> service.create(booker.getId(), bookingDto)
        );
        assertEquals(
                String.format(MessageHolder.AVAILABLE_NOT_AVAILABLE, bookingDto.getItemId()),
                exception.getMessage()
        );
    }

    @Test
    void testCreateWithOwnerForYourSelf() {
        item.setAvailable(false);
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.create(user.getId(), bookingDto)
        );
        assertEquals(
                String.format(MessageHolder.OWNER_ITEM, user.getId()),
                exception.getMessage()
        );
    }

    @Test
    void testCreateWithNullStart() {
        bookingDto.setStart(null);
        final NotValidDateException exception = assertThrows(
                NotValidDateException.class,
                () -> service.create(user.getId(), bookingDto)
        );
        assertEquals(MessageHolder.START_IS_NUll, exception.getMessage());
    }

    @Test
    void testCreateWithNullEnd() {
        bookingDto.setEnd(null);
        final NotValidDateException exception = assertThrows(
                NotValidDateException.class,
                () -> service.create(user.getId(), bookingDto)
        );
        assertEquals(MessageHolder.END_IS_NUll, exception.getMessage());
    }

    @Test
    void testCreateWithStartInPast() {
        String startInstr = "2022-07-07 11:30:40";
        start = LocalDateTime.parse(startInstr, formatter);
        bookingDto.setStart(start);
        final NotValidDateException exception = assertThrows(
                NotValidDateException.class,
                () -> service.create(user.getId(), bookingDto)
        );
        assertEquals(MessageHolder.START_IN_PAST, exception.getMessage());
    }

    @Test
    void testCreateWithEndInPast() {
        String endInstr = "2022-07-07 11:30:40";
        end = LocalDateTime.parse(endInstr, formatter);
        bookingDto.setEnd(end);
        final NotValidDateException exception = assertThrows(
                NotValidDateException.class,
                () -> service.create(user.getId(), bookingDto)
        );
        assertEquals(MessageHolder.END_IN_PAST, exception.getMessage());
    }

    @Test
    void testCreateWithEndBeforeStart() {
        String endInstr = "2023-06-07 10:30:40";
        end = LocalDateTime.parse(endInstr, formatter);
        bookingDto.setEnd(end);
        final NotValidDateException exception = assertThrows(
                NotValidDateException.class,
                () -> service.create(user.getId(), bookingDto)
        );
        assertEquals(MessageHolder.END_BEFORE_START, exception.getMessage());
    }

    @Test
    void testCreateWithEndEqualsStart() {
        String endInstr = "2023-06-07 11:30:40";
        end = LocalDateTime.parse(endInstr, formatter);
        bookingDto.setEnd(end);
        final NotValidDateException exception = assertThrows(
                NotValidDateException.class,
                () -> service.create(user.getId(), bookingDto)
        );
        assertEquals(MessageHolder.START_EQUAL_END, exception.getMessage());
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
    void testGetWithOtherUser() {
        em.persist(booking);
        em.flush();
        final NotValidOwnerException exception = assertThrows(
                NotValidOwnerException.class,
                () -> service.get(otherUser.getId(), booking.getId())
        );
        assertEquals(
                String.format(MessageHolder.BOOKER_OR_OWNER_ID_NOT_VALID, otherUser.getId(), booking.getItem().getId()),
                exception.getMessage()
        );
    }

    @Test
    void testAllByBooker() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
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
    void testGetAllByBookerWithPageParameter() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByBooker(booker.getId(),
                new Filter(new StateHolder("ALL"), new PageParameter(0, 3)));
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
    void testGetAllByBookerWithPageParameterWithApprovedState() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByBooker(booker.getId(),
                new Filter(new StateHolder("WAITING"), new PageParameter(0, 3)));
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
    void testGetAllByBookerWithPageParameterWithApprovedStateWithPastBooking() {
        String startInstr = "2022-05-05 11:30:40";
        String endInstr = "2022-07-05 11:50:40";
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        String startInstrForSecondBooking = "2022-08-05 11:30:40";
        String endInstrForSecondBooking = "2022-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByBooker(booker.getId(),
                new Filter(new StateHolder("PAST"), new PageParameter(0, 3)));
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
    void testGetAllByBookerWithPageParameterWithApprovedStateWithCurrentBooking() {
        String startInstr = "2023-05-05 11:30:40";
        String endInstr = "2023-07-05 11:50:40";
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        String startInstrForSecondBooking = "2023-05-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, itemTwo, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByBooker(booker.getId(),
                new Filter(new StateHolder("CURRENT"), new PageParameter(0, 3)));
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
    void testGetAllByBookerWithPageParameterWithApprovedStateWithFutureBooking() {
        String startInstr = "2023-07-05 11:30:40";
        String endInstr = "2023-07-05 11:50:40";
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        String startInstrForSecondBooking = "2023-07-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, itemTwo, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByBooker(booker.getId(),
                new Filter(new StateHolder("FUTURE"), new PageParameter(0, 3)));
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
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
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

    @Test
    void testGetAllByOwnerWithPageParameter() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByOwner(user.getId(),
                new Filter(new StateHolder("ALL"), new PageParameter(0, 3)));
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
    void testGetAllByOwnerWithPageParameterWithApprovedState() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByOwner(user.getId(),
                new Filter(new StateHolder("WAITING"), new PageParameter(0, 3)));
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
    void testGetAllByOwnerWithWronState() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        final UnSupportedStatusException exception = assertThrows(
                UnSupportedStatusException.class,
                () -> service.getAllByOwner(user.getId(),
                        new Filter(new StateHolder("UNSUPPORTED_STATE"), new PageParameter(0, 3)))
        );
        assertEquals(String.format(MessageHolder.UNSUPPORTED_STATUS, "UNSUPPORTED_STATE"), exception.getMessage());
    }

    @Test
    void testGetAllByOwnerWithWrongPageParameter() {
        String startInstrForSecondBooking = "2023-08-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        final NotValidParameterException exception = assertThrows(
                NotValidParameterException.class,
                () -> service.getAllByOwner(user.getId(),
                        new Filter(new StateHolder("ALL"), new PageParameter(-1, 3)))
        );
        assertEquals(String.format(MessageHolder.NOT_VALID_PARAMETER, -1), exception.getMessage());
    }

    @Test
    void testGetAllByOwnerWithPageParameterWithApprovedStateWithPastBooking() {
        String startInstr = "2022-05-05 11:30:40";
        String endInstr = "2022-07-05 11:50:40";
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        String startInstrForSecondBooking = "2022-08-05 11:30:40";
        String endInstrForSecondBooking = "2022-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, item, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByOwner(user.getId(),
                new Filter(new StateHolder("PAST"), new PageParameter(0, 3)));
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
    void testGetAllByOwnerWithPageParameterWithApprovedStateWithCurrentBooking() {
        String startInstr = "2023-05-05 11:30:40";
        String endInstr = "2023-07-05 11:50:40";
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        String startInstrForSecondBooking = "2023-05-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, itemTwo, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByOwner(user.getId(),
                new Filter(new StateHolder("CURRENT"), new PageParameter(0, 3)));
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
    void testGetAllByOwnerWithPageParameterWithApprovedStateWithFutureBooking() {
        String startInstr = "2023-07-05 11:30:40";
        String endInstr = "2023-07-05 11:50:40";
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        String startInstrForSecondBooking = "2023-07-05 11:30:40";
        String endInstrForSecondBooking = "2023-08-05 11:50:40";
        LocalDateTime startForSecondBooking = LocalDateTime.parse(startInstrForSecondBooking, formatter);
        LocalDateTime endForSecondBooking = LocalDateTime.parse(endInstrForSecondBooking, formatter);
        List<Booking> sourceBookings = List.of(
                makeBookingEntity(start, end, item, booker, BookingState.WAITING),
                makeBookingEntity(startForSecondBooking, endForSecondBooking, itemTwo, booker, BookingState.WAITING)
        );
        for (Booking booking : sourceBookings) {
            em.persist(booking);
        }
        em.flush();
        List<BookingDtoWithInfo> bookings = service.getAllByOwner(user.getId(),
                new Filter(new StateHolder("FUTURE"), new PageParameter(0, 3)));
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
    void testApproveByOwner() {
        em.persist(booking);
        em.flush();
        BookingDtoWithInfo result = service.approve(user.getId(), booking.getId(), true);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(bookingDto.getStart()));
        assertThat(result.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(result.getBooker(), equalTo(UserMapper.mapToUserDto(booker)));
        assertThat(result.getStatus(), equalTo(BookingState.APPROVED));
        assertThat(result.getItem(), equalTo(ItemMapper.mapToItemDto(item)));
    }

    @Test
    void testApproveIfRejectedAlready() {
        booking.setState(BookingState.REJECTED);
        em.persist(booking);
        em.flush();
        final NotValidDataForUpdateException exception = assertThrows(
                NotValidDataForUpdateException.class,
                () -> service.approve(user.getId(), booking.getId(), true)
        );
        assertEquals(String.format(MessageHolder.BOOKING_ALREADY_APPROVED, booking.getId()), exception.getMessage());
    }

    @Test
    void testApproveByBooker() {
        em.persist(booking);
        em.flush();
        final NotValidOwnerException exception = assertThrows(
                NotValidOwnerException.class,
                () -> service.approve(booker.getId(), booking.getId(), true)
        );
        assertEquals(String.format(MessageHolder.USER_ID_NOT_VALID, booker.getId(), booking.getItem().getId()),
                exception.getMessage()
        );
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

    private Item makeItemEntity(String name, String description, Boolean available, User user) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setUser(user);
        return item;
    }

    private ItemDtoWithBooking makeItemDtoWithBooking(String name, String description, Boolean available, long requestId) {
        ItemDtoWithBooking itemDtoWithBooking = new ItemDtoWithBooking();
        itemDtoWithBooking.setName(name);
        itemDtoWithBooking.setDescription(description);
        itemDtoWithBooking.setAvailable(available);
        itemDtoWithBooking.setRequestId(requestId);
        return itemDtoWithBooking;
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

    private BookingDto makeBookingDto(LocalDateTime dateTimeOne,
                                      LocalDateTime dateTimeTwo,
                                      long itemId) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(dateTimeOne);
        bookingDto.setEnd(dateTimeTwo);
        bookingDto.setItemId(itemId);
        return bookingDto;
    }
}