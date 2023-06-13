package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.PageRequestCustom;
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.request.dto.RequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    public static final Sort SORT_BY_CREATED_DESC = Sort.by("created").descending();
    public static final String DEFAULT_SIZE_OF_PAGE = "10";
    private final RequestService service;

    @PostMapping
    public RequestDto create(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody RequestDto requestDto) {
        log.info(String.format(MessageHolder.GET_CREATE_REQUEST), requestDto);
        return service.create(userId, requestDto);
    }

    @GetMapping("/{requestId}")
    public RequestDto get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long requestId) {
        log.info(String.format(MessageHolder.GET_REQUEST), requestId);
        return service.get(userId, requestId);
    }

    @GetMapping("/all")
    public List<RequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @RequestParam(defaultValue = "0") Integer from,
                                   @RequestParam(defaultValue = DEFAULT_SIZE_OF_PAGE) Integer size) {
        log.info(String.format(MessageHolder.GET_OWN_REQUESTS), userId);
        return service.getAll(userId, new PageRequestCustom(from, size, SORT_BY_CREATED_DESC));
    }

    @GetMapping
    public List<RequestDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info(String.format(MessageHolder.GET_OWN_REQUESTS), userId);
        return service.getOwnRequests(userId);
    }
}
