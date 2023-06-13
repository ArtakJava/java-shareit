package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoWithInfoJsonTest {
    private final JacksonTester<BookingDtoWithInfo> json;
    private LocalDateTime start;
    private LocalDateTime end;
    private DateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startInstr = "2023-06-07 11:30:40";
        String endInstr = "2023-06-07 11:50:40";
        start = LocalDateTime.parse(startInstr, formatter);
        end = LocalDateTime.parse(endInstr, formatter);
    }

    @Test
    void testItemDtoWithOutBookingSerialization() throws Exception {
        ItemDtoWithBooking itemDtoWithBooking = new ItemDtoWithBooking(
                1L,
                "name",
                "description",
                true,
                new BookingDtoWithBooker(),
                new BookingDtoWithBooker(),
                2L,
                new ArrayList<>()
        );
        BookingDtoWithInfo bookingDtoWithInfo = new BookingDtoWithInfo(
                1L,
                start,
                end,
                itemDtoWithBooking,
                new UserDto(),
                BookingState.WAITING
        );
        JsonContent<BookingDtoWithInfo> result = json.write(bookingDtoWithInfo);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(bookingDtoWithInfo.getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(bookingDtoWithInfo.getEnd().toString());
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(bookingDtoWithInfo.getStatus().toString());
    }

    @Test
    void testItemDtoWithOutBookingDeserialization() throws Exception {
        String jsonContent = "{" +
                "\"start\":\"2023-06-07T11:30:40\"," +
                "\"end\":\"2023-06-07T11:50:40\"," +
                "\"status\":\"WAITING\"" +
                "}";
        BookingDtoWithInfo bookingDtoWithInfo = new BookingDtoWithInfo();
        bookingDtoWithInfo.setStart(start);
        bookingDtoWithInfo.setEnd(end);
        bookingDtoWithInfo.setStatus(BookingState.WAITING);
        BookingDtoWithInfo result = json.parse(jsonContent).getObject();
        MatcherAssert.assertThat(result.getStart(), equalTo(bookingDtoWithInfo.getStart()));
        MatcherAssert.assertThat(result.getEnd(), equalTo(bookingDtoWithInfo.getEnd()));
        MatcherAssert.assertThat(result.getStatus(), equalTo(bookingDtoWithInfo.getStatus()));
    }
}