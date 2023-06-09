package ru.practicum.shareit;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageRequestCustom extends PageRequest {

    public PageRequestCustom(int from, int size, Sort sort) {
        super(from / size, size, sort);
    }
}