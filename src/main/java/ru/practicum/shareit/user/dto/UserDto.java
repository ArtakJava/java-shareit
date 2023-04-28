package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.messageManager.ErrorMessage;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private long id;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_NAME)
    private String name;
    @NotBlank
    @Email(message = ErrorMessage.USER_EMAIL)
    private String email;
}