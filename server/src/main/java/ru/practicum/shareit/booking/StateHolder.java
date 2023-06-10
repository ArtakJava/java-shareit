package ru.practicum.shareit.booking;

import lombok.Data;

@Data
public class StateHolder {
    private BookingState state;

    public StateHolder(String stateInStr) {
        this.state = getState(stateInStr);
    }

    private BookingState getState(String stateInStr) {
        return BookingState.valueOf(stateInStr);
    }
}