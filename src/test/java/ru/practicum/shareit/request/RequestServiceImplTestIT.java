package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceImplTestIT {
    private final RequestService service;
    private final EntityManager em;
    private RequestDto requestDto;
    private User user;
    private LocalDateTime dateTime;

    @BeforeEach
    void setUp() {
        user = makeUserEntity("Ivan", "ivan@email");
        em.persist(user);
        em.flush();
        String str = "2016-03-04 11:30:40";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTime = LocalDateTime.parse(str, formatter);
        requestDto = makeRequestDto("Request N1", dateTime.toString(), new ArrayList<>());
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

    private User makeUserEntity(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private RequestDto makeRequestDto(String description, String created, List<ItemDtoWithOutBooking> items) {
        RequestDto requestDto = new RequestDto();
        requestDto.setDescription(description);
        requestDto.setCreated(created);
        requestDto.setItems(items);
        return requestDto;
    }
}