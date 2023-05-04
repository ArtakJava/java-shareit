package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.DataEntity;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class Item extends DataEntity {
    public User owner;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_DESCRIPTION)
    public String description;
    @NotNull
    public Boolean available;
}