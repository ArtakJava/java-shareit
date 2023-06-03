package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private final UserService service;
    private final EntityManager em;
    private User user;
    private User userPatched;
    private UserDto userDto;
    private UserDto userDtoPatch;

    @BeforeEach
    void setUp() {
        user = makeUserEntity("Ivan", "ivan@email");
        userDto = makeUserDto("Ivan", "ivan@email");
        userDtoPatch = makeUserPatch("Dima", "dima@email");
        userPatched = makeUserEntity("Dima", "dima@email");
    }

    @Test
    void testCreate() {
        UserDto result = service.create(userDto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(userDto.getName()));
        assertThat(result.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void testGet() {
        em.persist(user);
        em.flush();
        UserDto result = service.get(user.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(userDto.getName()));
        assertThat(result.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void testGetAll() {
        List<UserDto> sourceUsers = List.of(
                makeUserDto("Ivan", "ivan@email"),
                makeUserDto("Petr", "petr@email"),
                makeUserDto("Vasilii", "vasilii@email")
        );
        for (UserDto userDto : sourceUsers) {
            User entity = UserMapper.mapToUserEntity(userDto);
            em.persist(entity);
        }
        em.flush();
        List<UserDto> users = service.getAll();
        assertThat(users, hasSize(sourceUsers.size()));
        for (UserDto userDto : sourceUsers) {
            assertThat(users, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(userDto.getName())),
                    hasProperty("email", equalTo(userDto.getEmail())
                    ))));
        }
    }

    @Test
    void testUpdate() {
        em.persist(user);
        em.flush();
        UserDto result = service.update(user.getId(), userDtoPatch);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(userPatched.getName()));
        assertThat(result.getEmail(), equalTo(userPatched.getEmail()));
    }

    @Test
    void testUpdateWithBD() {
        service.create(userDto);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail())
                .getSingleResult();
        service.update(user.getId(), userDtoPatch);
        TypedQuery<User> queryAfterUpdate = em.createQuery("Select u from User u where u.id = :id", User.class);
        User userUpdated = queryAfterUpdate.setParameter("id", user.getId())
                .getSingleResult();
        assertThat(userUpdated.getId(), notNullValue());
        assertThat(userUpdated.getName(), equalTo(userPatched.getName()));
        assertThat(userUpdated.getEmail(), equalTo(userPatched.getEmail()));
    }

    @Test
    void delete() {
        em.persist(user);
        em.flush();
        UserDto result = service.get(user.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(userDto.getName()));
        assertThat(result.getEmail(), equalTo(userDto.getEmail()));
        service.delete(user.getId());
        final JpaObjectRetrievalFailureException exception = assertThrows(
                JpaObjectRetrievalFailureException.class,
                () -> service.get(user.getId())
        );
        assertEquals(
                String.format("Unable to find ru.practicum.shareit.user.model.User with id %s; " +
                        "nested exception is javax.persistence.EntityNotFoundException: " +
                        "Unable to find ru.practicum.shareit.user.model.User with id %s", user.getId(), user.getId()),
                exception.getMessage()
        );
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

    private UserDto makeUserPatch(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }
}