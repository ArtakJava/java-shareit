package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.PageParameter;
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService service;

    @PostMapping
    public RequestDto create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody RequestDto requestDto) {
        log.info(String.format(MessageHolder.GET_CREATE_REQUEST), requestDto);
        return service.create(userId, requestDto);
    }

    @GetMapping("/{requestId}")
    public RequestDto get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long requestId) {
        log.info(String.format(MessageHolder.GET_REQUEST), requestId);
        return service.get(userId, requestId);
    }

    @GetMapping
    public List<RequestDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info(String.format(MessageHolder.GET_OWN_REQUESTS), userId);
        return service.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<RequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                   @RequestParam(required = false) Integer from,
                                   @RequestParam(required = false) Integer size) {
        log.info(String.format(MessageHolder.GET_OWN_REQUESTS), userId);
        return service.getAll(userId, new PageParameter(from, size));
    }
}
