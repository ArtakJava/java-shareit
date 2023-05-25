package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.exception.UnSupportedStatusException;
import ru.practicum.shareit.messageManager.ErrorMessage;

@Data
public class StateHolder {
    private BookingState state;

    public StateHolder(String stateInStr) {
        this.state = getState(stateInStr);
    }

    private BookingState getState(String stateInStr) {
        BookingState state;
        try {
            state = BookingApproveState.valueOf(stateInStr);
        } catch (IllegalArgumentException e1) {
            try {
                state = BookingTempState.valueOf(stateInStr);
            } catch (IllegalArgumentException e2) {
                throw new UnSupportedStatusException(String.format(ErrorMessage.UNSUPPORTED_STATUS, stateInStr));
            }
        }
        return state;
    }
}