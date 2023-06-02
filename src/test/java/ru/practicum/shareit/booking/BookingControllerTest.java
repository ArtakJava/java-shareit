package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig({BookingController.class})
@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    @InjectMocks
    private BookingController bookingController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private ItemDtoWithOutBooking itemDtoOne;
    private ItemDtoWithOutBooking itemDtoTwo;
    private ItemDtoWithBooking itemDtoWithBookingOne;
    private ItemDtoWithBooking itemDtoWithBookingTwo;
    private BookingDto bookingDtoOne;
    private BookingDto bookingDtoTwo;
    private BookingDtoWithInfo bookingDtoWithInfo;
    private BookingDtoWithInfo bookingDtoWithInfoTwo;
    private RequestDto requestDtoOne;
    private RequestDto requestDtoTwo;
    private CommentDto commentDto;
    private UserDto owner;
    private UserDto user;
    private UserDto requestor;
    private DateTimeFormatter formatter;

    @Autowired
    BookingControllerTest(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .build();
        owner = new UserDto(
                1L,
                "John",
                "john.doe@mail.com");
        user = new UserDto(
                2L,
                "Sona",
                "sona.mat@mail.com");
        requestor = new UserDto(
                3L,
                "Anton",
                "anton.bag@mail.com");
        requestDtoOne = new RequestDto(
                1L,
                "request 1",
                "2023-05-04 11:30:40",
                new ArrayList<>());
        requestDtoTwo = new RequestDto(
                2L,
                "request 2",
                "2023-05-04 12:30:40",
                new ArrayList<>());
        itemDtoOne = new ItemDtoWithOutBooking(
                1L,
                "item 1",
                "description 1",
                true,
                requestDtoOne.getId());
        itemDtoTwo = new ItemDtoWithOutBooking(
                2L,
                "item 2",
                "description 2",
                true,
                requestDtoTwo.getId());
        itemDtoWithBookingOne = new ItemDtoWithBooking(
                1L,
                "item 1",
                "description 1",
                true,
                new BookingDtoWithBooker(1L, requestor.getId()),
                new BookingDtoWithBooker(2L, requestor.getId()),
                requestor.getId(),
                new ArrayList<>());
        itemDtoWithBookingTwo = new ItemDtoWithBooking(
                2L,
                "item 2",
                "description 2",
                true,
                new BookingDtoWithBooker(1L, requestor.getId()),
                new BookingDtoWithBooker(2L, requestor.getId()),
                requestor.getId(),
                new ArrayList<>());
        commentDto = new CommentDto(1L, "text", requestor.getName(), "2023-05-04 12:30:40");
        String startInstr = "2023-06-05T11:30:40";
        String endInstr = "2023-06-05T11:50:40";
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        bookingDtoOne = new BookingDto(1L, start, end, itemDtoWithBookingOne.getId(), BookingState.WAITING);
        String startInstrTwo = "2023-06-05T11:30:40";
        String endInstrTwo = "2023-06-05T11:50:40";
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime startTwo = LocalDateTime.parse(startInstrTwo, formatter);
        LocalDateTime endTwo = LocalDateTime.parse(endInstrTwo, formatter);
        bookingDtoTwo = new BookingDto(2L, startTwo, endTwo, itemDtoWithBookingTwo.getId(), BookingState.WAITING);
        bookingDtoWithInfo = new BookingDtoWithInfo(1L, start, end, itemDtoWithBookingOne, user, BookingState.WAITING);
        bookingDtoWithInfoTwo = new BookingDtoWithInfo(2L, startTwo, endTwo, itemDtoWithBookingTwo, requestor, BookingState.WAITING);
    }

    @Test
    void testCreate() throws Exception {
        Mockito
                .when(bookingService.create(user.getId(), bookingDtoOne))
                .thenReturn(bookingDtoWithInfo);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoOne))
                        .header("X-Sharer-User-Id", user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoWithInfo.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDtoWithInfo.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoWithInfo.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDtoWithInfo.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDtoWithInfo.getItem().getName())))
                .andExpect(jsonPath("$.item.lastBooking.id",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.item.lastBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.item.nextBooking.id",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.item.nextBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.item.requestId",
                        is(bookingDtoWithInfo.getItem().getRequestId()), Long.class))
                .andExpect(jsonPath("$.item.available", is(bookingDtoWithInfo.getItem().getAvailable())))
                .andExpect(jsonPath("$.item.comments", is(bookingDtoWithInfo.getItem().getComments())))
                .andExpect(jsonPath("$.status", is(bookingDtoWithInfo.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoWithInfo.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDtoWithInfo.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email", is(bookingDtoWithInfo.getBooker().getEmail())));
    }

    @Test
    void testGet() throws Exception {
        Mockito
                .when(bookingService.get(anyLong(), anyLong()))
                .thenReturn(bookingDtoWithInfo);
        mvc.perform(get("/bookings/" + bookingDtoOne.getId())
                        .content(mapper.writeValueAsString(bookingDtoOne))
                        .header("X-Sharer-User-Id", user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoWithInfo.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDtoWithInfo.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoWithInfo.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDtoWithInfo.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDtoWithInfo.getItem().getName())))
                .andExpect(jsonPath("$.item.lastBooking.id",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.item.lastBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.item.nextBooking.id",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.item.nextBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.item.requestId",
                        is(bookingDtoWithInfo.getItem().getRequestId()), Long.class))
                .andExpect(jsonPath("$.item.available", is(bookingDtoWithInfo.getItem().getAvailable())))
                .andExpect(jsonPath("$.item.comments", is(bookingDtoWithInfo.getItem().getComments())))
                .andExpect(jsonPath("$.status", is(bookingDtoWithInfo.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoWithInfo.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDtoWithInfo.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email", is(bookingDtoWithInfo.getBooker().getEmail())));
    }

    @Test
    void testGetAllByBooker() throws Exception {
        List<BookingDtoWithInfo> bookings = new ArrayList<>(List.of(bookingDtoWithInfoTwo));
        Mockito
                .when(bookingService.getAllByBooker(anyLong(), any()))
                .thenReturn(bookings);
        mvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", requestor.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDtoWithInfoTwo.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDtoWithInfoTwo.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoWithInfoTwo.getEnd().toString())))
                .andExpect(jsonPath("$[0].item.id", is(bookingDtoWithInfoTwo.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingDtoWithInfoTwo.getItem().getName())))
                .andExpect(jsonPath("$[0].item.lastBooking.id",
                        is(bookingDtoWithInfoTwo.getItem().getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.lastBooking.bookerId",
                        is(bookingDtoWithInfoTwo.getItem().getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].item.nextBooking.id",
                        is(bookingDtoWithInfoTwo.getItem().getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.nextBooking.bookerId",
                        is(bookingDtoWithInfoTwo.getItem().getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].item.requestId",
                        is(bookingDtoWithInfoTwo.getItem().getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].item.available", is(bookingDtoWithInfoTwo.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.comments", is(bookingDtoWithInfoTwo.getItem().getComments())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoWithInfoTwo.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDtoWithInfoTwo.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(bookingDtoWithInfoTwo.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email", is(bookingDtoWithInfoTwo.getBooker().getEmail())));
    }

    @Test
    void testGetAllByOwner() throws Exception {
        List<BookingDtoWithInfo> bookings = new ArrayList<>(List.of(bookingDtoWithInfo, bookingDtoWithInfoTwo));
        Mockito
                .when(bookingService.getAllByOwner(anyLong(), any()))
                .thenReturn(bookings);
        mvc.perform(get("/bookings/owner?state=ALL")
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDtoWithInfo.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDtoWithInfo.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoWithInfo.getEnd().toString())))
                .andExpect(jsonPath("$[0].item.id", is(bookingDtoWithInfo.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingDtoWithInfo.getItem().getName())))
                .andExpect(jsonPath("$[0].item.lastBooking.id",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.lastBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].item.nextBooking.id",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.nextBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].item.requestId",
                        is(bookingDtoWithInfo.getItem().getRequestId()), Long.class))
                .andExpect(jsonPath("$[0].item.available", is(bookingDtoWithInfo.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.comments", is(bookingDtoWithInfo.getItem().getComments())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoWithInfo.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDtoWithInfo.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(bookingDtoWithInfo.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email", is(bookingDtoWithInfo.getBooker().getEmail())))
                .andExpect(jsonPath("$[1].id", is(bookingDtoWithInfoTwo.getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(bookingDtoWithInfoTwo.getStart().toString())))
                .andExpect(jsonPath("$[1].end", is(bookingDtoWithInfoTwo.getEnd().toString())))
                .andExpect(jsonPath("$[1].item.id", is(bookingDtoWithInfoTwo.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookingDtoWithInfoTwo.getItem().getName())))
                .andExpect(jsonPath("$[1].item.lastBooking.id",
                        is(bookingDtoWithInfoTwo.getItem().getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.lastBooking.bookerId",
                        is(bookingDtoWithInfoTwo.getItem().getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[1].item.nextBooking.id",
                        is(bookingDtoWithInfoTwo.getItem().getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.nextBooking.bookerId",
                        is(bookingDtoWithInfoTwo.getItem().getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[1].item.requestId",
                        is(bookingDtoWithInfoTwo.getItem().getRequestId()), Long.class))
                .andExpect(jsonPath("$[1].item.available", is(bookingDtoWithInfoTwo.getItem().getAvailable())))
                .andExpect(jsonPath("$[1].item.comments", is(bookingDtoWithInfoTwo.getItem().getComments())))
                .andExpect(jsonPath("$[1].status", is(bookingDtoWithInfoTwo.getStatus().toString())))
                .andExpect(jsonPath("$[1].booker.id", is(bookingDtoWithInfoTwo.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.name", is(bookingDtoWithInfoTwo.getBooker().getName())))
                .andExpect(jsonPath("$[1].booker.email", is(bookingDtoWithInfoTwo.getBooker().getEmail())));
    }

    @Test
    void testApprove() throws Exception {
        bookingDtoWithInfo.setStatus(BookingState.APPROVED);
        bookingDtoWithInfo.getItem().setAvailable(false);
        Mockito
                .when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDtoWithInfo);
        mvc.perform(patch("/bookings/" + bookingDtoWithInfo.getId() + "?approved=true")
                        .content(mapper.writeValueAsString(bookingDtoOne))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoWithInfo.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDtoWithInfo.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoWithInfo.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDtoWithInfo.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDtoWithInfo.getItem().getName())))
                .andExpect(jsonPath("$.item.lastBooking.id",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$.item.lastBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.item.nextBooking.id",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$.item.nextBooking.bookerId",
                        is(bookingDtoWithInfo.getItem().getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$.item.requestId",
                        is(bookingDtoWithInfo.getItem().getRequestId()), Long.class))
                .andExpect(jsonPath("$.item.available", is(bookingDtoWithInfo.getItem().getAvailable())))
                .andExpect(jsonPath("$.item.comments", is(bookingDtoWithInfo.getItem().getComments())))
                .andExpect(jsonPath("$.status", is(bookingDtoWithInfo.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoWithInfo.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDtoWithInfo.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email", is(bookingDtoWithInfo.getBooker().getEmail())));
    }

    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/bookings/" + bookingDtoWithInfo.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}