package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.RequestDto;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestDtoJsonTest {
    private final JacksonTester<RequestDto> json;

    @Test
    void testRequestDtoSerialization() throws Exception {
        RequestDto requestDto = new RequestDto(1L, "description1", "2023-02-02T11:00:00", new ArrayList<>());
        JsonContent<RequestDto> result = json.write(requestDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(requestDto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(requestDto.getCreated());
    }

    @Test
    void testRequestDtoDeserialization() throws Exception {
        String jsonContent = "{\"description\":\"Хотел бы воспользоваться щёткой для обуви\"}";
        RequestDto requestDto = new RequestDto();
        requestDto.setDescription("Хотел бы воспользоваться щёткой для обуви");
        RequestDto result = json.parse(jsonContent).getObject();
        MatcherAssert.assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
    }
}