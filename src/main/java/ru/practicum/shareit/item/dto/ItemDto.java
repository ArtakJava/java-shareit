package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.messageManager.ErrorMessage;

import javax.validation.constraints.NotBlank;

@Data
public class ItemDto {
    private long ownerId;
    private long id;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_NAME)
    private String name;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_DESCRIPTION)
    private String description;
    private boolean isAvailable;

    @JsonCreator
    @Builder
    public ItemDto(@JsonProperty(value = "ownerId") long ownerId,
                   @JsonProperty(value = "id") long id,
                   @JsonProperty(value = "name") String name,
                   @JsonProperty(value = "description") String description,
                   @JsonProperty(value = "available", required = true) boolean isAvailable) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        this.description = description;
        this.isAvailable = isAvailable;
    }
}