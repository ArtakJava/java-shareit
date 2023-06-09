package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.messageManager.MessageHolder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoWithOutBooking {
    private long id;
    @NotBlank(message = MessageHolder.ITEM_EMPTY_NAME)
    private String name;
    @NotBlank(message = MessageHolder.ITEM_EMPTY_DESCRIPTION)
    private String description;
    @NotNull
    private Boolean available;
    private Long requestId;
}