package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Filter {
    private StateHolder stateHolder;
    private PageParameter pageParameter;
}