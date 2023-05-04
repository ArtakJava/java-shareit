package ru.practicum.shareit.user.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.DataEntity;
import ru.practicum.shareit.messageManager.ErrorMessage;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class User extends DataEntity {
    @NotBlank
    @Email(message = ErrorMessage.USER_EMAIL)
    private String email;
}