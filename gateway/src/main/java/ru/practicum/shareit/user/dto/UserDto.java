package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.messageManager.MessageHolder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private long id;
    @NotBlank(message = MessageHolder.ITEM_EMPTY_NAME)
    private String name;
    @NotBlank
    @Email(message = MessageHolder.USER_EMAIL)
    private String email;
}