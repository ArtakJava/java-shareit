package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.PageParameter;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {
    @InjectMocks
    private final RequestService service;
    private final EntityManager em;
    @Mock
    private final RequestRepository requestRepository;
    @Mock
    private final UserRepository userRepository;
    private Request request;
    private RequestDto requestDto;
    private User user;
    private UserDto userDto;
    private LocalDateTime dateTime;
    private ItemDtoWithOutBooking itemDto;

    @BeforeEach
    void setUp() {
        user = makeUserEntity("Ivan", "ivan@email");
        em.persist(user);
        em.flush();
        itemDto = makeItemDto("item N1", "description", true);
        userDto = makeUserDto("Ivan", "ivan@email");
        String str = "2016-03-04 11:30:40";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTime = LocalDateTime.parse(str, formatter);
        requestDto = makeRequestDto("Request N1", dateTime.toString(), new ArrayList<>());
    }

    @Test
    void testCreate() {
        RequestDto result = service.create(user.getId(), requestDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.getCreated(), equalTo(requestDto.getCreated()));
    }

    @Test
    void testCreateDB() {
        service.create(user.getId(), requestDto);
        TypedQuery<Request> query = em.createQuery("Select u from Request u where u.description = :description", Request.class);
        Request result = query.setParameter("description", requestDto.getDescription()).getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.getCreated().toString(), equalTo(requestDto.getCreated()));
    }

    @Test
    void testGet() {
        request = makeRequestEntity("Request N1", dateTime, user);
        em.persist(request);
        em.flush();
        RequestDto result = service.get(user.getId(), request.getId());
        List<RequestDto> requests = service.getAll(user.getId(), new PageParameter(null, null));
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
            em.persist(entity);
            RequestDto result = service.get(user.getId(), entity.getId());
            sourceRequests.add(entity);
        }
        em.flush();
        Mockito
                .when(requestRepository.findAllByRequestorIdNot(anyLong(), (Sort) any()))
                .thenReturn(sourceRequests);
        Mockito
                .when(requestRepository.findAllByRequestorIdNot(
                        user.getId(), PageRequest.of(0, 3, Sort.by("created").descending())))
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
    void testGetAll() {
        em.persist(user);
        List<RequestDto> sourceRequestsDto = List.of(
                makeRequestDto("Request N1", dateTime.toString(), new ArrayList<>()),
                makeRequestDto("Request N2", dateTime.toString(), new ArrayList<>()),
                makeRequestDto("Request N3", dateTime.toString(), new ArrayList<>())
        );
        List<Request> sourceRequests = new ArrayList<>();
        for (RequestDto requestDto : sourceRequestsDto) {
            Request entity = RequestMapper.mapToRequestEntity(requestDto, user);
            em.persist(entity);
            RequestDto result = service.get(user.getId(), entity.getId());
            sourceRequests.add(entity);
        }
        em.flush();
        Mockito
                .when(requestRepository.findAllByRequestorIdNot(anyLong(), (Sort) any()))
                .thenReturn(sourceRequests);
        Mockito
                .when(requestRepository.findAllByRequestorIdNot(
                        user.getId(), PageRequest.of(0, 3, Sort.by("created").descending())))
                .thenReturn(sourceRequests);
        List<RequestDto> requests = service.getAll(user.getId(), new PageParameter(0, 3));
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

    private RequestDto makeRequestDto(String description, String created, List<ItemDtoWithOutBooking> items) {
        RequestDto requestDto = new RequestDto();
        requestDto.setDescription(description);
        requestDto.setCreated(created);
        requestDto.setItems(items);
        return requestDto;
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
}