package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig({ItemController.class})
@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {
    @MockBean
    private ItemService itemService;
    @InjectMocks
    private ItemController itemController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private ItemDtoWithOutBooking itemDtoOne;
    private ItemDtoWithOutBooking itemDtoTwo;
    private ItemDtoWithBooking itemDtoWithBookingOne;
    private ItemDtoWithBooking itemDtoWithBookingTwo;
    private RequestDto requestDtoOne;
    private RequestDto requestDtoTwo;
    private CommentDto commentDto;
    private UserDto owner;
    private UserDto user;
    private UserDto requestor;

    @Autowired
    ItemControllerTest(ItemService itemService) {
        this.itemService = itemService;
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(itemController)
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
    }

    @Test
    void testCreate() throws Exception {
        Mockito
                .when(itemService.create(owner.getId(), itemDtoOne))
                .thenReturn(itemDtoWithBookingOne);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoOne))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoWithBookingOne.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoWithBookingOne.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoWithBookingOne.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoWithBookingOne.getAvailable())))
                .andExpect(jsonPath("$.comments", is(itemDtoWithBookingOne.getComments())));
    }

    @Test
    void testGet() throws Exception {
        Mockito
                .when(itemService.get(anyLong(), anyLong()))
                .thenReturn(itemDtoWithBookingOne);
        mvc.perform(get("/items/" + itemDtoWithBookingOne.getId())
                        .content(mapper.writeValueAsString(itemDtoWithBookingOne))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoWithBookingOne.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoWithBookingOne.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoWithBookingOne.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoWithBookingOne.getAvailable())))
                .andExpect(jsonPath("$.comments", is(itemDtoWithBookingOne.getComments())));
    }

    @Test
    void testGetAllByUser() throws Exception {
        List<ItemDtoWithBooking> itemsDto = new ArrayList<>(List.of(itemDtoWithBookingOne));
        Mockito
                .when(itemService.getAllByUser(requestDtoOne.getId()))
                .thenReturn(itemsDto);
        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(itemsDto))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDtoWithBookingOne.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoWithBookingOne.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoWithBookingOne.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoWithBookingOne.getAvailable())))
                .andExpect(jsonPath("$[0].comments", is(itemDtoWithBookingOne.getComments())));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito
                .when(itemService.update(anyLong(), anyLong(), any()))
                .thenReturn(itemDtoWithBookingOne);
        mvc.perform(patch("/items/" + itemDtoOne.getId())
                        .content(mapper.writeValueAsString(itemDtoOne))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoWithBookingOne.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoWithBookingOne.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoWithBookingOne.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoWithBookingOne.getAvailable())))
                .andExpect(jsonPath("$.comments", is(itemDtoWithBookingOne.getComments())));
    }

    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/items/" + itemDtoOne.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testSearch() throws Exception {
        List<ItemDtoWithBooking> itemsDto = new ArrayList<>(List.of(itemDtoWithBookingOne));
        Mockito
                .when(itemService.search(anyString()))
                .thenReturn(itemsDto);
        mvc.perform(get("/items/search?text=description 1")
                        .content(mapper.writeValueAsString(itemsDto))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDtoWithBookingOne.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoWithBookingOne.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoWithBookingOne.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoWithBookingOne.getAvailable())))
                .andExpect(jsonPath("$[0].comments", is(itemDtoWithBookingOne.getComments())));
    }

    @Test
    void testCreateComment() throws Exception {
        Mockito
                .when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);
        mvc.perform(post("/items/" + itemDtoOne.getId() + "/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", owner.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }
}