package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.DataEntity;
import ru.practicum.shareit.messageManager.ErrorMessage;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ItemEntity extends DataEntity {
    private long ownerId;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_DESCRIPTION)
    private String description;
    @NotNull
    private boolean isAvailable;
}