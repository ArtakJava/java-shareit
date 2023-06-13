package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoWithOutBookingJsonTest {
    private final JacksonTester<ItemDtoWithOutBooking> json;

    @Test
    void testItemDtoWithOutBookingSerialization() throws Exception {
        ItemDtoWithOutBooking itemDto = new ItemDtoWithOutBooking(
                1L,
                "item name",
                "description",
                true,
                3L
        );
        JsonContent<ItemDtoWithOutBooking> result = json.write(itemDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
    }

    @Test
    void testItemDtoWithOutBookingDeserialization() throws Exception {
        String jsonContent = "{\"name\":\"Отвертка\", \"description\":\"Аккумуляторная отвертка\", \"available\":\"true\"}";
        ItemDtoWithOutBooking itemDtoWithOutBooking = new ItemDtoWithOutBooking();
        itemDtoWithOutBooking.setName("Отвертка");
        itemDtoWithOutBooking.setDescription("Аккумуляторная отвертка");
        itemDtoWithOutBooking.setAvailable(true);
        ItemDtoWithOutBooking result = json.parse(jsonContent).getObject();
        MatcherAssert.assertThat(result.getName(), equalTo(itemDtoWithOutBooking.getName()));
        MatcherAssert.assertThat(result.getDescription(), equalTo(itemDtoWithOutBooking.getDescription()));
        MatcherAssert.assertThat(result.getAvailable(), equalTo(itemDtoWithOutBooking.getAvailable()));
    }
}