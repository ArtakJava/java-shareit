package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.exception.NotValidParameterException;
import ru.practicum.shareit.messageManager.ErrorMessage;

@Setter
@Getter
@AllArgsConstructor
public class PageParameter {
    private Integer from;
    private Integer size;

    public boolean isPresent() {
        return from != null && size != null;
    }

    public Integer getPage() {
        if (from < 0 || size < 0) {
            throw new NotValidParameterException(
                    String.format(ErrorMessage.NOT_VALID_PARAMETER, from < 0 ? from : size)
            );
        }
        return from / size;
    }
}