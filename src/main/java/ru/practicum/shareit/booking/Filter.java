package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.PageRequestCustom;

@AllArgsConstructor
@Getter
@Setter
public class Filter {
    private StateHolder stateHolder;
    private PageRequestCustom pageRequest;
}