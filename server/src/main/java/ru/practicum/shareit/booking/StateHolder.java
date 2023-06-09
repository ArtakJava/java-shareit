package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.exception.UnSupportedStatusException;
import ru.practicum.shareit.messageManager.MessageHolder;

@Data
public class StateHolder {
    private BookingState state;

    public StateHolder(String stateInStr) {
        this.state = getState(stateInStr);
    }

    private BookingState getState(String stateInStr) {
        BookingState state;
        try {
            state = BookingState.valueOf(stateInStr);
        } catch (IllegalArgumentException e1) {
            throw new UnSupportedStatusException(String.format(MessageHolder.UNSUPPORTED_STATUS, stateInStr));
        }
        return state;
    }
}