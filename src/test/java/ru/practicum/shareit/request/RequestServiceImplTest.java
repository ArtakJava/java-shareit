package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.PageRequestCustom;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
public class RequestServiceImplTest {
    private static final Sort SORT_BY_CREATED_DESC = Sort.by("created").descending();
    @InjectMocks
    private final RequestService service;
    private final EntityManager em;
    @MockBean
    private final RequestRepository requestRepository;
    @MockBean
    private final UserRepository userRepository;
    private Request request;
    private RequestDto requestDto;
    private User user;
    private LocalDateTime dateTime;
    private ItemDtoWithOutBooking itemDto;

    @BeforeEach
    void setUp() {
        user = makeUserEntity("Ivan", "ivan@email");
        em.persist(user);
        em.flush();
        itemDto = makeItemDto("item N1", "description", true);
        String str = "2016-03-04 11:30:40";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTime = LocalDateTime.parse(str, formatter);
        requestDto = makeRequestDto("Request N1", dateTime.toString(), new ArrayList<>());
    }

    @Test
    void testCreate() {
        request = makeRequestEntity("Request N1", dateTime, user);
        Mockito
                .when(userRepository.getReferenceById(anyLong()))
                .thenReturn(user);
        Mockito
                .when(requestRepository.save(any()))
                .thenReturn(request);
        RequestDto result = service.create(user.getId(), requestDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.getCreated(), equalTo(requestDto.getCreated()));
    }

    @Test
    void testGet() {
        request = makeRequestEntity("Request N1", dateTime, user);
        em.persist(request);
        em.flush();
        Mockito
                .when(userRepository.getReferenceById(anyLong()))
                .thenReturn(user);
        Mockito
                .when(requestRepository.getReferenceById(anyLong()))
                .thenReturn(request);
        RequestDto result = service.get(user.getId(), request.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.getCreated(), equalTo(requestDto.getCreated()));
    }

    @Test
    void testGetOwnRequests() {
        em.persist(user);
        List<RequestDto> sourceRequestsDto = List.of(
                makeRequestDto("Request N1", dateTime.toString(), new ArrayList<>(List.of(itemDto))),
                makeRequestDto("Request N3", dateTime.toString(), new ArrayList<>(List.of(itemDto)))
        );
        List<Request> sourceRequests = new ArrayList<>();
        for (RequestDto requestDto : sourceRequestsDto) {
            Request entity = RequestMapper.mapToRequestEntity(requestDto, user);
            sourceRequests.add(entity);
        }
        Mockito
                .when(userRepository.getReferenceById(anyLong()))
                .thenReturn(user);
        Mockito
                .when(requestRepository.findAllByRequestorId(anyLong(), any()))
                .thenReturn(sourceRequests);
        List<RequestDto> requests = service.getOwnRequests(user.getId());
        assertThat(requests, hasSize(sourceRequestsDto.size()));
        for (RequestDto requestDto : sourceRequestsDto) {
            assertThat(requests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(requestDto.getDescription())),
                    hasProperty("created", equalTo(requestDto.getCreated()))
            )));
        }
    }

    @Test
    void testGetAllWithPageParameter() {
        em.persist(user);
        List<RequestDto> sourceRequestsDto = List.of(
                makeRequestDto("Request N1", dateTime.toString(), new ArrayList<>()),
                makeRequestDto("Request N2", dateTime.toString(), new ArrayList<>()),
                makeRequestDto("Request N3", dateTime.toString(), new ArrayList<>())
        );
        List<Request> sourceRequests = new ArrayList<>();
        for (RequestDto requestDto : sourceRequestsDto) {
            Request entity = RequestMapper.mapToRequestEntity(requestDto, user);
            sourceRequests.add(entity);
        }
        Mockito
                .when(userRepository.getReferenceById(anyLong()))
                .thenReturn(user);
        Mockito
                .when(requestRepository.findAllByRequestorIdNot(anyLong(), any()))
                .thenReturn(sourceRequests);
        List<RequestDto> requests = service.getAll(
                user.getId(), new PageRequestCustom(0, 3, SORT_BY_CREATED_DESC)
        );
        assertThat(requests, hasSize(sourceRequestsDto.size()));
        for (RequestDto requestDto : sourceRequestsDto) {
            assertThat(requests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(requestDto.getDescription())),
                    hasProperty("created", equalTo(requestDto.getCreated()))
                    )));
        }
    }

    private User makeUserEntity(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Request makeRequestEntity(String description, LocalDateTime created, User requestor) {
        Request request = new Request();
        request.setDescription(description);
        request.setCreated(created);
        request.setRequestor(requestor);
        return request;
    }

    private RequestDto makeRequestDto(String description, String created, List<ItemDtoWithOutBooking> items) {
        RequestDto requestDto = new RequestDto();
        requestDto.setDescription(description);
        requestDto.setCreated(created);
        requestDto.setItems(items);
        return requestDto;
    }

    private ItemDtoWithOutBooking makeItemDto(String name, String description, Boolean available) {
        ItemDtoWithOutBooking itemDto = new ItemDtoWithOutBooking();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }
}