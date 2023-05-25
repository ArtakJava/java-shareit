package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfo;
import ru.practicum.shareit.exception.UnSupportedStatusException;
import ru.practicum.shareit.messageManager.InfoMessage;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService service;

    @PostMapping
    public BookingInfo create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody BookingDto bookingDto) {
        log.info(InfoMessage.GET_CREATE_REQUEST, bookingDto);
        return service.create(userId, bookingDto);
    }

    @GetMapping("/{bookingId}")
    public BookingInfo get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long bookingId) {
        log.info(InfoMessage.GET_REQUEST, bookingId);
        return service.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingInfo> getAllByBooker(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestParam(required = false, defaultValue = "ALL")
                                          String state) {
        log.info(InfoMessage.GET_ALL_REQUEST);
        return service.getAllByBooker(userId, new StateHolder(state));
    }

    @GetMapping("/owner")
    public List<BookingInfo> getAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(required = false, defaultValue = "ALL")
                                           String state) throws UnSupportedStatusException {
        log.info(InfoMessage.GET_ALL_REQUEST);
        return service.getAllByOwner(userId, new StateHolder(state));
    }

    @PatchMapping("/{bookingId}")
    public BookingInfo approve(@RequestHeader("X-Sharer-User-Id") long userId,
                               @PathVariable long bookingId,
                               @RequestParam(name = "approved") boolean isApproved) {
        log.info(InfoMessage.GET_UPDATE_REQUEST, bookingId);
        return service.approve(userId, bookingId, isApproved);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        log.info(InfoMessage.GET_UPDATE_REQUEST, userId);
        service.delete(userId);
    }
}