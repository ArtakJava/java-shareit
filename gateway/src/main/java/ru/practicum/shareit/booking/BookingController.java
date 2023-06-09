package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.UnSupportedStatusException;
import ru.practicum.shareit.messageManager.MessageHolder;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	public static final String DEFAULT_SIZE_OF_PAGE = "10";
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
												@RequestBody @Valid BookItemRequestDto bookingDto) {
		log.info(MessageHolder.GET_CREATE_REQUEST, bookingDto);
		return bookingClient.createBooking(userId, bookingDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
											 @PathVariable Long bookingId) {
		log.info(MessageHolder.GET_REQUEST, bookingId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
											  @RequestParam(name = "state", defaultValue = "all") String stateParam,
											  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
											  @Positive @RequestParam(name = "size", defaultValue = DEFAULT_SIZE_OF_PAGE) Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new UnSupportedStatusException(String.format(MessageHolder.UNSUPPORTED_STATUS, stateParam)));
		log.info(MessageHolder.GET_ALL_REQUEST);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnBookings(@RequestHeader("X-Sharer-User-Id") long userId,
												 @RequestParam(name = "state", defaultValue = "all") String stateParam,
												 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
												 @Positive @RequestParam(name = "size", defaultValue = DEFAULT_SIZE_OF_PAGE) Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new UnSupportedStatusException(String.format(MessageHolder.UNSUPPORTED_STATUS, stateParam)));
		log.info(MessageHolder.GET_ALL_REQUEST);
		return bookingClient.getOwnBookings(userId, state, from, size);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@RequestHeader("X-Sharer-User-Id") long userId,
										  @PathVariable long bookingId,
										  @RequestParam(name = "approved") boolean isApproved) {
		log.info(MessageHolder.GET_UPDATE_REQUEST, bookingId);
		return bookingClient.approve(userId, bookingId, isApproved);
	}
}