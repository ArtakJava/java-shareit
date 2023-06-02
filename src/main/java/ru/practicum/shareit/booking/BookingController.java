package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.messageManager.MessageHolder;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService service;

    @PostMapping
    public BookingDtoWithInfo create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody BookingDto bookingDto) {
        log.info(MessageHolder.GET_CREATE_REQUEST, bookingDto);
        return service.create(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoWithInfo get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        log.info(MessageHolder.GET_REQUEST, bookingId);
        return service.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoWithInfo> getAllByBooker(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(required = false) Integer from,
                                                   @RequestParam(required = false) Integer size) {
        log.info(MessageHolder.GET_ALL_REQUEST);
        return service.getAllByBooker(userId, new Filter(new StateHolder(state), new PageParameter(from, size)));
    }

    @GetMapping("/owner")
    public List<BookingDtoWithInfo> getAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(required = false) Integer from,
                                                  @RequestParam(required = false) Integer size) {
        log.info(MessageHolder.GET_ALL_REQUEST);
        return service.getAllByOwner(userId, new Filter(new StateHolder(state), new PageParameter(from, size)));
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoWithInfo approve(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @PathVariable long bookingId,
                                      @RequestParam(name = "approved") boolean isApproved) {
        log.info(MessageHolder.GET_UPDATE_REQUEST, bookingId);
        return service.approve(userId, bookingId, isApproved);
    }
}