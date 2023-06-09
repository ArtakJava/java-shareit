package ru.practicum.shareit.user;

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

@SpringJUnitWebConfig({UserController.class})
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @MockBean
    private UserService userService;
    @InjectMocks
    private UserController userController;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;
    private UserDto userDtoOne;
    private UserDto userDtoTwo;

    @Autowired
    UserControllerTest(UserService userService) {
        this.userService = userService;
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

        userDtoOne = new UserDto(
                1L,
                "John",
                "john.doe@mail.com");
        userDtoTwo = new UserDto(
                2L,
                "Ivan",
                "ivan.dem@mail.com");
    }

    @Test
    void testCreate() throws Exception {
        Mockito
                .when(userService.create(any()))
                .thenReturn(userDtoOne);
        mvc.perform(post("/users")
                .content(mapper.writeValueAsString(userDtoOne))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoOne.getName())))
                .andExpect(jsonPath("$.email", is(userDtoOne.getEmail())));
    }

    @Test
    void testGet() throws Exception {
        Mockito
                .when(userService.get(anyLong()))
                .thenReturn(userDtoOne);
        mvc.perform(get("/users/" + userDtoOne.getId())
                        .content(mapper.writeValueAsString(userDtoOne))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoOne.getName())))
                .andExpect(jsonPath("$.email", is(userDtoOne.getEmail())));
    }

    @Test
    void testGetAll() throws Exception {
        List<UserDto> usersDto = new ArrayList<>(List.of(userDtoOne, userDtoTwo));
        Mockito
                .when(userService.getAll())
                .thenReturn(usersDto);
        mvc.perform(get("/users")
                        .content(mapper.writeValueAsString(usersDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(userDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDtoOne.getName())))
                .andExpect(jsonPath("$[0].email", is(userDtoOne.getEmail())))
                .andExpect(jsonPath("$[1].id", is(userDtoTwo.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(userDtoTwo.getName())))
                .andExpect(jsonPath("$[1].email", is(userDtoTwo.getEmail())));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito
                .when(userService.update(anyLong(), any()))
                .thenReturn(userDtoOne);
        mvc.perform(patch("/users/" + userDtoOne.getId())
                        .content(mapper.writeValueAsString(userDtoOne))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoOne.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDtoOne.getName())))
                .andExpect(jsonPath("$.email", is(userDtoOne.getEmail())));
    }

    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/users/" + userDtoOne.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}