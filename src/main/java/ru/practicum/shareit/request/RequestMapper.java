package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static RequestDto mapToRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated().toString())
                .build();
    }

    public static Request mapToRequestEntity(RequestDto requestDto, User requestor) {
        return Request.builder()
                .id(requestDto.getId())
                .description(requestDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.parse(requestDto.getCreated()))
                .build();
    }

    public static List<RequestDto> mapRequestsDtoWithItems(List<RequestDto> requests, Map<Long, List<Item>> itemsByRequest) {
        requests.forEach(request -> request.setItems(
                itemsByRequest.getOrDefault(request.getId(), new ArrayList<>()).stream()
                        .map(ItemMapper::mapToItemDtoWithOutBooking)
                        .collect(Collectors.toList())));
        return requests;
    }

    public static RequestDto mapRequestDtoWithItems(RequestDto request, List<Item> items) {
        request.setItems(items.stream()
                .map(ItemMapper::mapToItemDtoWithOutBooking)
                .collect(Collectors.toList()));
        return request;
    }
}