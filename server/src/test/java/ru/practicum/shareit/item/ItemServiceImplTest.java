package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotValidOwnerForUpdateException;
import ru.practicum.shareit.exception.UnBookingCommentException;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(locations = "classpath:test.properties")
public class ItemServiceImplTest {
    private final ItemService service;
    private final EntityManager em;
    private User user;
    private User otherUser;
    private Item item;
    private Item itemPatched;
    private ItemDtoWithOutBooking itemDto;
    private ItemDtoWithOutBooking itemDtoPatch;
    private LocalDateTime dateTime;

    @BeforeEach
    void setUp() {
        String str = "2023-06-05 11:30:40";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTime = LocalDateTime.parse(str, formatter);
        user = makeUserEntity("Ivan", "ivan@email");
        em.persist(user);
        em.flush();
        otherUser = makeUserEntity("Dima", "dima@email");
        em.persist(user);
        em.flush();
        RequestDto requestDto = makeRequestDto(1L, "Request N1", dateTime.toString(), new ArrayList<>());
        item = makeItemEntity("item N1", "description", true, user);
        itemDto = makeItemDto("item N1", "description", true, requestDto.getId());
        itemDtoPatch = makeItemDtoPatch("item N1 update");
        itemPatched = makeItemEntity("item N1 update", "description", true, user);
    }

    @Test
    void testCreate() {
        ItemDtoWithBooking result = service.create(user.getId(), itemDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void testGet() {
        em.persist(item);
        em.flush();
        ItemDtoWithBooking result = service.get(user.getId(), item.getId());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void testGetAllByUser() {
        List<Item> sourceItems = List.of(
                makeItemEntity("item N1", "description1", true, user),
                makeItemEntity("item N2", "description2", true, user),
                makeItemEntity("item N3", "description3", true, user)
        );
        for (Item item : sourceItems) {
            em.persist(item);
        }
        em.flush();
        List<ItemDtoWithBooking> items = service.getAllByUser(user.getId());
        assertThat(items, hasSize(sourceItems.size()));
        for (Item item : sourceItems) {
            assertThat(items, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable()))
            )));
        }
    }

    @Test
    void testUpdate() {
        ItemDtoWithBooking result = service.create(user.getId(), itemDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        result = service.update(user.getId(), result.getId(), itemDtoPatch);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(itemPatched.getName()));
        assertThat(result.getDescription(), equalTo(itemPatched.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemPatched.getAvailable()));
    }

    @Test
    void testUpdateByOtherUser() {
        ItemDtoWithBooking result = service.create(user.getId(), itemDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        em.persist(otherUser);
        em.flush();
        final NotValidOwnerForUpdateException exception = assertThrows(
                NotValidOwnerForUpdateException.class,
                () -> service.update(otherUser.getId(), result.getId(), itemDtoPatch)
        );
        assertEquals(String.format(MessageHolder.USER_ID_NOT_VALID, otherUser.getId(), result.getId()), exception.getMessage());
    }

    @Test
    void testUpdateBD() {
        service.create(user.getId(), itemDto);
        TypedQuery<Item> query = em.createQuery("Select u from Item u where u.name = :name", Item.class);
        Item item = query.setParameter("name", itemDto.getName())
                .getSingleResult();
        service.update(user.getId(), item.getId(), itemDtoPatch);
        TypedQuery<Item> queryAfterUpdate = em.createQuery("Select u from Item u where u.id = :id", Item.class);
        Item itemUpdated = queryAfterUpdate.setParameter("id", item.getId())
                .getSingleResult();
        assertThat(itemUpdated.getId(), notNullValue());
        assertThat(itemUpdated.getName(), equalTo(itemPatched.getName()));
        assertThat(itemUpdated.getDescription(), equalTo(itemPatched.getDescription()));
        assertThat(itemUpdated.getAvailable(), equalTo(itemPatched.getAvailable()));
    }

    @Test
    void delete() {
        item = makeItemEntity("item N1", "description", true, user);
        em.persist(item);
        em.flush();
        ItemDtoWithBooking result = service.get(user.getId(), item.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        service.delete(user.getId(), item.getId());
        final JpaObjectRetrievalFailureException exception = assertThrows(
                JpaObjectRetrievalFailureException.class,
                () -> service.get(user.getId(), item.getId())
        );
        assertEquals(
                String.format("Unable to find ru.practicum.shareit.item.model.Item with id %s; " +
                                "nested exception is javax.persistence.EntityNotFoundException: " +
                                "Unable to find ru.practicum.shareit.item.model.Item with id %s", item.getId(), item.getId()),
                exception.getMessage()
        );
    }

    @Test
    void testSearch() {
        List<Item> sourceItems = List.of(
                makeItemEntity("item NUMBER1", "description1", true, user),
                makeItemEntity("item NUMBER2", "description2", true, user),
                makeItemEntity("item N3", "description3", true, user)
        );
        for (Item item : sourceItems) {
            em.persist(item);
        }
        em.flush();
        List<ItemDtoWithBooking> items = service.search("item NUMBER");
        assertThat(items, hasSize(sourceItems.size() - 1));
        for (Item item : sourceItems) {
            assertThat(items, hasItem(anyOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable()))
            )));
        }
    }

    @Test
    void testCreateComment() {
        em.persist(item);
        em.flush();
        String startInstr = "2023-05-05 11:30:40";
        String endInstr = "2023-05-05 11:50:40";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startInstr, formatter);
        LocalDateTime end = LocalDateTime.parse(endInstr, formatter);
        Booking booking = makeBookingEntity(start, end, item, user);
        em.persist(booking);
        em.flush();
        CommentDto commentDto = makeCommentDto("text comment 1", user.getName(), dateTime);
        CommentDto result = service.createComment(user.getId(), item.getId(), commentDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getText(), equalTo(commentDto.getText()));
        assertThat(result.getAuthorName(), equalTo(user.getName()));
        assertThat(result.getCreated(), equalTo(commentDto.getCreated()));
    }

    @Test
    void testCreateCommentWithOutBooking() {
        em.persist(item);
        em.flush();
        CommentDto commentDto = makeCommentDto("text comment 1", user.getName(), dateTime);
        final UnBookingCommentException exception = assertThrows(
                UnBookingCommentException.class,
                () -> service.createComment(user.getId(), item.getId(), commentDto)
        );
        assertEquals(String.format(MessageHolder.AUTHOR_NOT_BOOKING, user.getId(), item.getId()), exception.getMessage());
    }

    private User makeUserEntity(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item makeItemEntity(String name, String description, Boolean available, User user) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setUser(user);
        return item;
    }

    private ItemDtoWithOutBooking makeItemDto(String name, String description, Boolean available, long requestId) {
        ItemDtoWithOutBooking itemDto = new ItemDtoWithOutBooking();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }

    private ItemDtoWithOutBooking makeItemDtoPatch(String name) {
        ItemDtoWithOutBooking itemDto = new ItemDtoWithOutBooking();
        itemDto.setName(name);
        return itemDto;
    }



    private Booking makeBookingEntity(LocalDateTime dateTimeOne,
                                      LocalDateTime dateTimeTwo,
                                      Item item,
                                      User user) {
        Booking booking = new Booking();
        booking.setStart(dateTimeOne);
        booking.setEnd(dateTimeTwo);
        booking.setItem(item);
        booking.setBooker(user);
        return booking;
    }

    private CommentDto makeCommentDto(String text, String authorName, LocalDateTime dateTimeOne) {
        CommentDto commentDto = new CommentDto();
        commentDto.setText(text);
        commentDto.setAuthorName(authorName);
        commentDto.setCreated(dateTimeOne.toString());
        return commentDto;
    }

    private RequestDto makeRequestDto(Long id, String description, String created, List<ItemDtoWithOutBooking> items) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(id);
        requestDto.setDescription(description);
        requestDto.setCreated(created);
        requestDto.setItems(items);
        return requestDto;
    }
}