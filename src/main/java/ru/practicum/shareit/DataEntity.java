package ru.practicum.shareit;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.messageManager.ErrorMessage;

import javax.validation.constraints.NotBlank;

@Data
@SuperBuilder
@Accessors(chain = true)
public abstract class DataEntity {
    private long id;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_NAME)
    private String name;
}