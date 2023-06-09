package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig({RequestController.class})
@ExtendWith(MockitoExtension.class)
public class RequestControllerTest {
    @MockBean
    private RequestService requestService;
    @InjectMocks
    private RequestController requestController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private RequestDto requestDtoOne;
    private RequestDto requestDtoTwo;
    private UserDto owner;
    private UserDto user;

    @Autowired
    RequestControllerTest(RequestService requestService) {
        this.requestService = requestService;
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .build();

        owner = new UserDto(
                1L,
                "John",
                "john.doe@mail.com");

        user = new UserDto(
                1L,
                "John",
                "john.doe@mail.com");

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
    }

    @Test
    void testCreate() throws Exception {
        Mockito
                .when(requestService.create(owner.getId(), requestDtoOne))
                .thenReturn(requestDtoOne);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDtoOne))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDtoOne.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDtoOne.getCreated())))
                .andExpect(jsonPath("$.items", is(requestDtoOne.getItems())));
    }

    @Test
    void testGet() throws Exception {
        Mockito
                .when(requestService.get(anyLong(), anyLong()))
                .thenReturn(requestDtoOne);
        mvc.perform(get("/requests/" + requestDtoOne.getId())
                        .content(mapper.writeValueAsString(requestDtoOne))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDtoOne.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDtoOne.getCreated())))
                .andExpect(jsonPath("$.items", is(requestDtoOne.getItems())));
    }

    @Test
    void testGetOwnRequests() throws Exception {
        List<RequestDto> requestsDto = new ArrayList<>(List.of(requestDtoOne, requestDtoTwo));
        Mockito
                .when(requestService.getOwnRequests(owner.getId()))
                .thenReturn(requestsDto);
        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(requestsDto))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(requestDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDtoOne.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestDtoOne.getCreated())))
                .andExpect(jsonPath("$[0].items", is(requestDtoOne.getItems())))
                .andExpect(jsonPath("$[1].id", is(requestDtoTwo.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(requestDtoTwo.getDescription())))
                .andExpect(jsonPath("$[1].created", is(requestDtoTwo.getCreated())))
                .andExpect(jsonPath("$[1].items", is(requestDtoTwo.getItems())));
    }

    @Test
    void testGetAll() throws Exception {
        List<RequestDto> requestsDto = new ArrayList<>(List.of(requestDtoOne, requestDtoTwo));
        Mockito
                .when(requestService.getAll(anyLong(), any()))
                .thenReturn(requestsDto);
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(requestsDto))
                        .header("X-Sharer-User-Id", user.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(requestDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDtoOne.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestDtoOne.getCreated())))
                .andExpect(jsonPath("$[0].items", is(requestDtoOne.getItems())))
                .andExpect(jsonPath("$[1].id", is(requestDtoTwo.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(requestDtoTwo.getDescription())))
                .andExpect(jsonPath("$[1].created", is(requestDtoTwo.getCreated())))
                .andExpect(jsonPath("$[1].items", is(requestDtoTwo.getItems())));
    }
}