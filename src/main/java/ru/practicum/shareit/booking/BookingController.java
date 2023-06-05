package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.PageRequestCustom;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoWithInfo;
import ru.practicum.shareit.messageManager.MessageHolder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    public static final Sort SORT_BY_START_DESC = Sort.by("start").descending();
    public static final String DEFAULT_SIZE_OF_PAGE = "10";
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
                                                   @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                   @RequestParam(defaultValue = DEFAULT_SIZE_OF_PAGE) @Min(0) Integer size) {
        log.info(MessageHolder.GET_ALL_REQUEST);
        return service.getAllByBooker(
                userId, new Filter(new StateHolder(state), new PageRequestCustom(from, size, SORT_BY_START_DESC))
        );
    }

    @GetMapping("/owner")
    public List<BookingDtoWithInfo> getAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                  @RequestParam(defaultValue = DEFAULT_SIZE_OF_PAGE) @Min(0) Integer size) {
        log.info(MessageHolder.GET_ALL_REQUEST);
        return service.getAllByOwner(
                userId, new Filter(new StateHolder(state), new PageRequestCustom(from, size, SORT_BY_START_DESC))
        );
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoWithInfo approve(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @PathVariable long bookingId,
                                      @RequestParam(name = "approved") boolean isApproved) {
        log.info(MessageHolder.GET_UPDATE_REQUEST, bookingId);
        return service.approve(userId, bookingId, isApproved);
    }
}