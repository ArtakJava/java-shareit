package ru.practicum.shareit.request;

import ru.practicum.shareit.booking.PageParameter;
import ru.practicum.shareit.request.dto.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto create(long userId, RequestDto requestDto);

    List<RequestDto> getOwnRequests(long userId);

    List<RequestDto> getAll(long userId, PageParameter pageParameter);

    RequestDto get(long userId, long requestId);
}